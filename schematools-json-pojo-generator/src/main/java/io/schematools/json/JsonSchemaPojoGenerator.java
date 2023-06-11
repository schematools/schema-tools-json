package io.schematools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

public class JsonSchemaPojoGenerator {

    private final Configuration configuration;
    private final SchemaLocator schemaLocator = new SchemaLocator();
    private Map<Id, JsonSchema> jsonSchemaMap;

    public JsonSchemaPojoGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public synchronized void generate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonSchemaMap = new HashMap<>();
            List<Path> paths = schemaLocator.getAllFilePaths(configuration.sourcePath());
            for (Path path: paths) {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                Id id = Id.create(rootNode.get("$id").asText());
                JavaClassSource javaClassSource = Roaster.create(JavaClassSource.class).setPackage(id.packageName()).setName(id.className());
                jsonSchemaMap.put(id, new JsonSchema(id, rootNode, javaClassSource));
            }
            for (Map.Entry<Id, JsonSchema> entry : jsonSchemaMap.entrySet()) {
                walkJsonTree(entry.getValue());
                write(configuration.targetPath(), entry.getKey(), entry.getValue().getJavaClassSource());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void walkJsonTree(JsonSchema jsonSchema) {
        NodeType nodeType = determineNodeType(jsonSchema.getRootNode());
        switch (nodeType) {
            case OBJECT -> handleObjectNodeType(jsonSchema.getId(),null, jsonSchema.getRootNode(), jsonSchema.getJavaClassSource());
            default -> throw new UnsupportedOperationException("Unimplemented");
        }
        jsonSchema.setProcessed(true);
    }

    public void handleObjectNodeType(Id id, String jsonPropertyName, JsonNode objectNode, JavaClassSource javaClassSource) {
        if (!Objects.isNull(jsonPropertyName)) {
            JavaClassSource innerJavaSourceClass = Roaster.create(JavaClassSource.class)
                    .setName(CaseHelper.convertToCamelCase(jsonPropertyName, true));
            Set<Map.Entry<String, JsonNode>> propertyNodes = objectNode.get("properties").properties();
            for (Map.Entry<String, JsonNode> entry : propertyNodes) {
                walkJsonNode(null, entry.getKey(), entry.getValue(), innerJavaSourceClass);
            }
            javaClassSource.addNestedType(innerJavaSourceClass);
            FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                    .setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                    .setType(innerJavaSourceClass)
                    .setPublic();
            addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
        } else {
            Set<Map.Entry<String, JsonNode>> propertyNodes = objectNode.get("properties").properties();
            for (Map.Entry<String, JsonNode> entry : propertyNodes) {
                walkJsonNode(id, entry.getKey(), entry.getValue(), javaClassSource);
            }
        }
    }

    public void write(String targetPath, Id id, JavaClassSource javaClassSource) throws FileNotFoundException {
        File file = new File(id.outputFileName(targetPath));
        file.getParentFile().mkdirs();
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.write(javaClassSource.toString());
        printWriter.close();
    }

    public void walkJsonNode(Id id, String jsonNodeName, JsonNode currentNode, JavaClassSource javaClassSource) {
        NodeType nodeType = determineNodeType(currentNode);
        switch (nodeType) {
            case STRING -> handleStringNodeType(jsonNodeName, javaClassSource);
            case NUMBER -> handleNumberNodeType(jsonNodeName, javaClassSource);
            case INTEGER -> handleIntegerNodeType(jsonNodeName, javaClassSource);
            case BOOLEAN -> handleBooleanNodeType(jsonNodeName, javaClassSource);
            case OBJECT -> handleObjectNodeType(id, jsonNodeName, currentNode, javaClassSource);
            case ARRAY -> handleArrayNodeType(id, jsonNodeName, currentNode, javaClassSource);
            case REFERENCE -> handleReferenceNodeType(id, jsonNodeName, currentNode, javaClassSource);
        }
    }

    public void handleStringNodeType(String jsonPropertyName, JavaClassSource javaClassSource) {
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField().setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                .setType(String.class)
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
    }

    public void handleNumberNodeType(String jsonPropertyName, JavaClassSource javaClassSource) {
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField().setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                .setType(Double.class)
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
    }

    public void handleIntegerNodeType(String jsonPropertyName, JavaClassSource javaClassSource) {
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField().setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                .setType(Integer.class)
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
    }

    public void handleBooleanNodeType(String jsonPropertyName, JavaClassSource javaClassSource) {
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                .setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                .setType(Boolean.class)
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
    }

    public void handleArrayNodeType(Id id, String jsonPropertyName, JsonNode arrayNode, JavaClassSource javaClassSource) {
        javaClassSource.addImport(List.class);
        JsonNode itemsNode = arrayNode.get("items");
        NodeType nodeType = determineNodeType(itemsNode);
        if (nodeType.equals(NodeType.REFERENCE)) {
            String ref = itemsNode.get("$ref").asText();
            //TODO check ref type
            String absoluteRef = id.baseUri() + ref;
            JsonSchema jsonSchema = jsonSchemaMap.get(Id.create(absoluteRef));
            walkJsonTree(jsonSchema);
            JavaSource<?> javaSource = jsonSchema.getJavaClassSource().getEnclosingType();
            javaSource.addImport(javaSource);
            FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                    .setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                    .setType(String.format("List<%s>", javaSource.getName()))
                    .setPublic();
            addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
        } else {
            Class<?> clazz = getClassForNodeType(nodeType);
            FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                    .setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                    .setType(String.format("List<%s>", clazz.getSimpleName()))
                    .setPublic();
            addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
        }
    }

    public Class<?> getClassForNodeType(NodeType nodeType) {
        return switch (nodeType) {
            case STRING -> String.class;
            case NUMBER -> Double.class;
            case INTEGER -> Integer.class;
            case BOOLEAN -> Boolean.class;
            default -> throw new UnsupportedOperationException("No class for nodeType[" + nodeType + "]");
        };
    }

    public void handleReferenceNodeType(Id id, String jsonNodeName, JsonNode currentNode, JavaClassSource javaClassSource) {
        String ref = currentNode.get("$ref").asText();
        //TODO check ref type
        String absoluteRef = id.baseUri() + ref;
        JsonSchema jsonSchema = jsonSchemaMap.get(Id.create(absoluteRef));
        walkJsonTree(jsonSchema);
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                .setName(CaseHelper.convertToCamelCase(jsonNodeName, false))
                .setType(jsonSchema.getJavaClassSource())
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonNodeName);
    }

    public void addJsonPropertyAnnotation(FieldSource<?> fieldSource, String propertyName) {
        fieldSource.addAnnotation(JsonProperty.class).setStringValue("value", propertyName);
    }

    public NodeType determineNodeType(JsonNode jsonNode) {
        if (jsonNode.has("type")) {
            String type = jsonNode.get("type").asText();
            return switch (type) {
                case "string" -> NodeType.STRING;
                case "number" -> NodeType.NUMBER;
                case "integer" -> NodeType.INTEGER;
                case "boolean" -> NodeType.BOOLEAN;
                case "object" -> NodeType.OBJECT;
                case "array" -> NodeType.ARRAY;
                default -> throw new RuntimeException("Unknown type: " + type);
            };
        }
        if (jsonNode.has("$ref")) {
            return NodeType.REFERENCE;
        }
        throw new RuntimeException("Unknown NodeType: " + jsonNode);
    }

    public record Configuration(String sourcePath, String targetPath) {
        public Configuration(String sourcePath) {
            this(sourcePath, "target/generated-sources");
        }
    }

}

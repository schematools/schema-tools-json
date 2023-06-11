package io.schematools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JsonSchemaPojoGenerator {

    private final Configuration configuration;
    private final SchemaLocator schemaLocator = new SchemaLocator();

    public JsonSchemaPojoGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void generate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Path> paths = schemaLocator.getAllFilePaths(configuration.sourcePath());
            for (Path path: paths) {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                Id id = Id.create(rootNode.get("$id").asText());
                JavaClassSource javaClassSource = Roaster.create(JavaClassSource.class).setPackage(id.packageName()).setName(id.className());
                walkJsonTree(rootNode, javaClassSource);
                write(configuration.targetPath(), id, javaClassSource);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void walkJsonTree(JsonNode rootNode, JavaClassSource javaClassSource) {
        NodeType nodeType = determineNodeType(rootNode);
        switch (nodeType) {
            case OBJECT -> handleObjectNodeType(null, rootNode, javaClassSource);
            default -> throw new UnsupportedOperationException("Unimplemented");
        }
    }

    public void handleObjectNodeType(String jsonPropertyName, JsonNode objectNode, JavaClassSource javaClassSource) {
        if (!Objects.isNull(jsonPropertyName)) {
            JavaClassSource innerJavaSourceClass = Roaster.create(JavaClassSource.class)
                    .setName(CaseHelper.convertToCamelCase(jsonPropertyName, true));
            Set<Map.Entry<String, JsonNode>> propertyNodes = objectNode.get("properties").properties();
            for (Map.Entry<String, JsonNode> entry : propertyNodes) {
                walkJsonNode(entry.getKey(), entry.getValue(), innerJavaSourceClass);
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
                walkJsonNode(entry.getKey(), entry.getValue(), javaClassSource);
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

    public void walkJsonNode(String jsonNodeName, JsonNode currentNode, JavaClassSource javaClassSource) {
        NodeType nodeType = determineNodeType(currentNode);
        switch (nodeType) {
            case STRING -> handleStringNodeType(jsonNodeName, javaClassSource);
            case NUMBER -> handleNumberNodeType(jsonNodeName, javaClassSource);
            case INTEGER -> handleIntegerNodeType(jsonNodeName, javaClassSource);
            case BOOLEAN -> handleBooleanNodeType(jsonNodeName, javaClassSource);
            case OBJECT -> handleObjectNodeType(jsonNodeName, currentNode, javaClassSource);
            case ARRAY -> handleArrayNodeType(jsonNodeName, currentNode, javaClassSource);
            case REFERENCE -> handleReferenceNodeType();
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

    public void handleArrayNodeType(String jsonPropertyName, JsonNode arrayNode, JavaClassSource javaClassSource) {
        javaClassSource.addImport(List.class);
        JsonNode itemsNode = arrayNode.get("items");
        NodeType nodeType = determineNodeType(itemsNode);
        Class<?> clazz = getClassForNodeType(nodeType);
        FieldSource<JavaClassSource> fieldSource = javaClassSource.addField()
                .setName(CaseHelper.convertToCamelCase(jsonPropertyName, false))
                .setType(String.format("List<%s>", clazz.getSimpleName()))
                .setPublic();
        addJsonPropertyAnnotation(fieldSource, jsonPropertyName);
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

    public void handleReferenceNodeType() {
        throw new UnsupportedOperationException("Cannot handle object");
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

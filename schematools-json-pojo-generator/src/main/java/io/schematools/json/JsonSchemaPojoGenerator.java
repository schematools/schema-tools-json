package io.schematools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class JsonSchemaPojoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaPojoGenerator.class);

    private final Configuration configuration;

    private final JsonSchemaLoader jsonSchemaLoader = new JsonSchemaLoader();

    private Map<Id, JsonSchema> jsonSchemaMap;

    public JsonSchemaPojoGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void generate() {
        this.jsonSchemaMap = jsonSchemaLoader.load(configuration.sourcePath);
        jsonSchemaMap.forEach((id, jsonSchema) -> process(jsonSchema));
        jsonSchemaMap.forEach((id, jsonSchema) -> write(jsonSchema, configuration.targetPath));
    }

    public void process(JsonSchema jsonSchema) {
        if (jsonSchema.isProcessed()) {
            return;
        }
        processProperties(jsonSchema);
        jsonSchema.setProcessed(true);
    }

    public void processProperties(JsonSchema jsonSchema) {
        for (Map.Entry<String, JsonNode> entry : jsonSchema.getRootNode().get("properties").properties()) {
            processProperty(entry.getKey(), entry.getValue(), jsonSchema);
        }
    }

    public void processProperty(String propertyName, JsonNode propertyNode, JsonSchema jsonSchema) {
        if (propertyNode.has("type")) {
            String type = propertyNode.get("type").asText();
            switch (type) {
                case "string" -> {
                    addField(propertyName, String.class, jsonSchema.getJavaClassSource());
                }
                case "integer" -> {
                    addField(propertyName, Integer.class, jsonSchema.getJavaClassSource());
                }
                case "array" -> {

                }
                default -> {
                    throw new RuntimeException("Unknown property type: " + type + " on " + propertyName);
                }
            }
        }
        if (propertyNode.has("$ref")) {
            addRef(propertyName, propertyNode, jsonSchema);
        }
    }

    public void addRef(String propertyName, JsonNode propertyNode, JsonSchema jsonSchema) {
        String ref = propertyNode.get("$ref").asText();
        String absRef = jsonSchema.getId().baseUri() + ref;
        JsonSchema childSchema = jsonSchemaMap.get(Id.create(absRef));
        process(childSchema);
        jsonSchema.getJavaClassSource().addField()
                .setName(CaseHelper.convertToCamelCase(propertyName, false))
                .setType(childSchema.getJavaClassSource().getName())
                .setPublic()
                .addAnnotation(JsonProperty.class)
                .setStringValue("value", propertyName);
    }

    public void addField(String propertyName, Class<?> clazz, JavaClassSource javaClassSource) {
        javaClassSource.addField()
                .setName(CaseHelper.convertToCamelCase(propertyName, false))
                .setType(clazz)
                .setPublic()
                .addAnnotation(JsonProperty.class)
                .setStringValue("value", propertyName);
    }

    public void write(JsonSchema jsonSchema, String targetPath) {
        File file = new File(jsonSchema.outputFileName(targetPath));
        file.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(jsonSchema.getJavaClassSource().toString());
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static class Configuration {
        public String sourcePath;
        public String targetPath;

        public Configuration(String sourcePath, String targetPath) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
        }

    }

}

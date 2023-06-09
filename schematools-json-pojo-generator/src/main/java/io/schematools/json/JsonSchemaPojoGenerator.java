package io.schematools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import io.schematools.json.generate.JsonSchema;
import io.schematools.json.generate.JsonSchemaLoader;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class JsonSchemaPojoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaPojoGenerator.class);

    private final JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration;
    private final JsonSchemaLoader jsonSchemaLoader = new JsonSchemaLoader();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<IdAdapter, JsonSchema> jsonSchemaMap = new HashMap<>();

    public JsonSchemaPojoGenerator(JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration) {
        this.jsonSchemaPojoGeneratorConfiguration = jsonSchemaPojoGeneratorConfiguration;
    }

    public void generate() {
        this.jsonSchemaMap.putAll(jsonSchemaLoader.load(jsonSchemaPojoGeneratorConfiguration.sourcePath(), jsonSchemaPojoGeneratorConfiguration.targetPath()));
        for (Map.Entry<IdAdapter, JsonSchema> entry : jsonSchemaMap.entrySet()) {
            entry.getValue().process();
            entry.getValue().write();
        }
    }



    private void generate(JsonSchema jsonSchema) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(jsonSchema.idAdapter().packageName());
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.addClass(jsonSchema.className());
        classOrInterfaceDeclaration.addAndGetAnnotation(Generated.class)
                .addPair("value", new StringLiteralExpr("io.schematools"));
        for(Map.Entry<String, JsonNode> entry : jsonSchema.rootNode().get("properties").properties()) {
            handleProperty(jsonSchema, entry.getKey(), entry.getValue(), classOrInterfaceDeclaration);
        }
        for (final JsonNode node : jsonSchema.rootNode().get("required")){
            this.handleRequired(node, classOrInterfaceDeclaration);
        }

        try {
            String outputPath = jsonSchemaPojoGeneratorConfiguration.targetPath() + "/" + jsonSchema.idAdapter().packageName().replace('.', '/');
            File file = new File(outputPath, jsonSchema.javaSourceFileName());
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(compilationUnit.toString());
            writer.close();
            System.out.println("Java source file generated successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("An error occurred while generating the Java source file: " + e.getMessage());
        }
    }

    private void handleProperty(JsonSchema jsonSchema, String jsonPropertyName, JsonNode jsonPropertyNode, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (jsonPropertyNode.has("$ref")) {
            String id = URI.create(jsonSchema.idAdapter().baseURI().toString() + jsonPropertyNode.get("$ref").asText()).toString();
            JsonSchema childSchema = jsonSchemaMap.get(id);
            this.generate(jsonSchema);
        }
        String type = jsonPropertyNode.get("type").asText();
        Class<?> clazz = getClassForType(type);
        FieldDeclaration fieldDeclaration = classOrInterfaceDeclaration.addField(clazz, CaseHelper.convertToCamelCase(jsonPropertyName, false), Modifier.Keyword.PUBLIC);
        fieldDeclaration.addAndGetAnnotation(JsonProperty.class)
                .addPair("value", new StringLiteralExpr(jsonPropertyName));
    }

    private void handleRequired(JsonNode jsonNode, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        FieldDeclaration fieldDeclaration = classOrInterfaceDeclaration.getFieldByName(CaseHelper.convertToCamelCase(jsonNode.asText(), false)).get();
        String type = fieldDeclaration.getVariable(0).getType().toString();
        fieldDeclaration.addAndGetAnnotation(getAnnotationForType(type));
    }

    public Class<?> getClassForType(String type) {
        switch (type) {
            case "string" -> {
                return String.class;
            }
            case "integer" -> {
                return Integer.class;
            }
            default -> throw new RuntimeException("Unknown Class for node type: " + type);
        }
    }

    public Class<? extends Annotation> getAnnotationForType(String type) {
        switch (type) {
            case "String" -> {
                return NotEmpty.class;
            }
            default -> {
                return NotNull.class;
            }
        }
    }

}

package io.schematools.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonSchemaPojoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaPojoGenerator.class);

    private final JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonSchemaPojoGenerator(JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration) {
        this.jsonSchemaPojoGeneratorConfiguration = jsonSchemaPojoGeneratorConfiguration;
    }

    public void generate() {
        try {
            List<Path> paths = this.getAllFilePaths(jsonSchemaPojoGeneratorConfiguration.sourcePath());
            List<JsonSchema> jsonSchemas = new ArrayList<>();
            for (Path path: paths) {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                IdAdapter idAdapter = IdAdapter.parse(rootNode.get("$id").asText());
                JsonSchema jsonSchema = new JsonSchema(path, rootNode, idAdapter);
                this.generate(jsonSchema);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generate(JsonSchema jsonSchema) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(jsonSchema.idAdapter().packageName());
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.addClass(jsonSchema.getClassName());
        classOrInterfaceDeclaration.addAndGetAnnotation(Generated.class)
                .addPair("value", new StringLiteralExpr("io.schematools"));
        for(Map.Entry<String, JsonNode> entry : jsonSchema.jsonNode().get("properties").properties()) {
            handleProperty(entry.getKey(), entry.getValue(), classOrInterfaceDeclaration);
        }
        for (final JsonNode node : jsonSchema.jsonNode().get("required")){
            this.handleRequired(node, classOrInterfaceDeclaration);
        }

        try {
            String outputPath = jsonSchemaPojoGeneratorConfiguration.targetPath() + "/" + jsonSchema.idAdapter().packageName().replace('.', '/');
            File file = new File(outputPath, jsonSchema.getJavaSourceFileName());
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(compilationUnit.toString());
            writer.close();
            System.out.println("Java source file generated successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("An error occurred while generating the Java source file: " + e.getMessage());
        }
    }

    private void handleProperty(String name, JsonNode current, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        String type = current.get("type").asText();
        Class<?> clazz = getClassForType(type);
        FieldDeclaration fieldDeclaration = classOrInterfaceDeclaration.addField(clazz, CaseHelper.convertToCamelCase(name, false), Modifier.Keyword.PUBLIC);
        fieldDeclaration.addAndGetAnnotation(JsonProperty.class)
                .addPair("value", new StringLiteralExpr(name));
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

    private List<Path> getAllFilePaths(String sourcePath) {
        try {
            return Files.walk(Path.of(sourcePath), Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

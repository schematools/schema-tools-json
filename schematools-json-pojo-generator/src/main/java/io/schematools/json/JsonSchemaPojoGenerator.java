package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
        compilationUnit.addClass(jsonSchema.getClassName());

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

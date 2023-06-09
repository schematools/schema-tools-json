package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonSchemaLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<IdAdapter, JsonSchema> load(String sourcePath) {
        try {
            Map<IdAdapter, JsonSchema> jsonSchemaMap = new HashMap<>();
            List<Path> paths = this.getAllFilePaths(sourcePath);
            for (Path path: paths) {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                IdAdapter idAdapter = IdAdapter.parse(rootNode.get("$id").asText());
                JsonSchema jsonSchema = JsonSchema.create(path, rootNode, idAdapter);
                jsonSchemaMap.put(idAdapter, jsonSchema);
            }
            return jsonSchemaMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
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

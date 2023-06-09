package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.annotation.processing.Generated;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonSchemaLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<Id, JsonSchema> load(String sourcePath) {
        Map<Id, JsonSchema> jsonSchemaMap = new HashMap<>();
        try {
            List<Path> paths = this.getAllFilePaths(sourcePath);
            for (Path path : paths) {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                Id id = Id.create(rootNode.get("$id").asText());
                JavaClassSource javaClassSource = initializeJavaClassSource(id);
                JsonSchema jsonSchema = new JsonSchema(id, rootNode, javaClassSource);
                jsonSchemaMap.put(id, jsonSchema);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonSchemaMap;
    }

    private JavaClassSource initializeJavaClassSource(Id id) {
        final JavaClassSource javaClassSource = Roaster.create(JavaClassSource.class);
        javaClassSource.setPackage(id.packageName()).setName(id.className());
        javaClassSource.addAnnotation(Generated.class).setStringValue("value", "io.schematools");
        return javaClassSource;
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

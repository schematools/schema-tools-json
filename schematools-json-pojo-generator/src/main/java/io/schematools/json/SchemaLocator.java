package io.schematools.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaLocator {

    public List<Path> getAllFilePaths(String sourcePath) {
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

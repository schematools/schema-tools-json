package io.schematools.json;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaLocator {

    public List<Path> getAllFilePaths(String sourcePath, String fileNameGlob) {
        try {
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNameGlob);
            return Files.walk(Path.of(sourcePath), Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PojoGenerator {

    public static final Logger logger = LoggerFactory.getLogger(PojoGenerator.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JCodeModel jCodeModel = new JCodeModel();

    public void generate(String sourcePath) {
        try {
            List<Path> paths = getAllFilePaths(sourcePath);
            for (Path path: paths) {
                JsonNode jsonNode = objectMapper.readTree(path.toFile());
                this.createClass(jsonNode);
            }
            jCodeModel.build(new File("target/generated-sources"));
        } catch (IOException | JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }

    public void createClass(JsonNode jsonNode) throws JClassAlreadyExistsException {
        Id id = new Id(jsonNode.get("$id").asText());
        jCodeModel._class(id.convertToFullyQualifiedName());
    }

    private List<Path> getAllFilePaths(String sourcePath) throws IOException {
        return Files.walk(Path.of(sourcePath), Integer.MAX_VALUE)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .peek(path -> logger.debug("Json Schema found at {}", path))
                .collect(Collectors.toList());
    }

}

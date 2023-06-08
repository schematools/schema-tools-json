package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public record JsonSchema(Path path, JsonNode jsonNode, IdAdapter idAdapter) {

    public String getClassName() {
        return idAdapter.className() + idAdapter.version().toUpperCase();
    }

    public String getJavaSourceFileName() {
        return this.getClassName() + ".java";
    }

}

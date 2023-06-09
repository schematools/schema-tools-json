package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public record JsonSchema(Id id, JsonNode rootNode, JavaClassSource javaClassSource) {

    public String outputFileName(String targetPath) {
        return targetPath + "/" + id.packageName().replace('.', '/') + "/" + id.className() + ".java";
    }

}

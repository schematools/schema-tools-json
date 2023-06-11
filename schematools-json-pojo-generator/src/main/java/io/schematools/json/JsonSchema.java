package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class JsonSchema {

    private final Id id;
    private final JsonNode rootNode;
    private final JavaClassSource javaClassSource;
    private boolean processed;

    public JsonSchema(Id id, JsonNode rootNode, JavaClassSource javaClassSource) {
        this.id = id;
        this.rootNode = rootNode;
        this.javaClassSource = javaClassSource;
    }

    public Id getId() {
        return id;
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public JavaClassSource getJavaClassSource() {
        return javaClassSource;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

}

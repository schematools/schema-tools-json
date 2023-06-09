package io.schematools.json;

public record JsonSchemaPojoGeneratorConfiguration(String sourcePath, String targetPath) {

    public static final String DEFAULT_TARGET_PATH = "target/generated-sources";

}

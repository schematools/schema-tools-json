package io.schematools.json.generate;

import io.schematools.json.JsonSchemaPojoGeneratorConfiguration;
import org.junit.jupiter.api.Test;

public class JsonSchemaPojoGeneratorTest {

    @Test
    void simple() {
        var jsonSchemaPojoGeneratorConfiguration = new JsonSchemaPojoGeneratorConfiguration("src/test/resources/schema/simple", JsonSchemaPojoGeneratorConfiguration.DEFAULT_TARGET_PATH);
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(jsonSchemaPojoGeneratorConfiguration);
        jsonSchemaPojoGenerator.generate();

    }

}

package io.schematools.json.generate;

import io.schematools.json.JsonSchemaPojoGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorTest {

    @Test
    void simple() {
        JsonSchemaPojoGenerator.Configuration configuration = new JsonSchemaPojoGenerator.Configuration("src/test/resources/schema/simple", "target/generated-sources");
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(configuration);
        jsonSchemaPojoGenerator.generate();
        assertThat(new File("target/generated-sources/com/example/schemas/AddressV1.java")).exists().isNotEmpty();
    }

}

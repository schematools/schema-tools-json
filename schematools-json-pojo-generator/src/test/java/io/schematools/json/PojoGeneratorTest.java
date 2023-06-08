package io.schematools.json;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class PojoGeneratorTest {

    @Test
    void simple() {
        PojoGenerator pojoGenerator = new PojoGenerator();
        pojoGenerator.generate("src/test/resources/schema/simple");
        assertThat(new File("target/generated-sources/com/example/schemas/AddressV1.java")).exists();
    }

}

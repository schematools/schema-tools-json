package io.schematools.json.generate;

import io.schematools.json.JsonSchemaPojoGenerator;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorBasicTest {

    @Test
    void basic() throws IOException {
        JsonSchemaPojoGenerator.Configuration configuration = new JsonSchemaPojoGenerator.Configuration("src/test/resources/schema/basic");
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(configuration);
        jsonSchemaPojoGenerator.generate();
        File expectedFile = new File("target/generated-sources/com/example/schemas/BasicV1.java");
        assertThat(expectedFile).exists().isNotEmpty();
        JavaClassSource javaClassSource = Roaster.parse(JavaClassSource.class, expectedFile);
        assertThat(javaClassSource.getPackage()).isEqualTo("com.example.schemas");
        assertThat(javaClassSource.getField("myString")).isNotNull();
    }

}

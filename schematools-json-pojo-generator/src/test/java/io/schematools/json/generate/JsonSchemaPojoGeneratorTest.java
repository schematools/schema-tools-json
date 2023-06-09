package io.schematools.json.generate;

import io.schematools.json.JsonSchemaPojoGenerator;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorTest {

    @Test
    void simple() throws IOException {
        JsonSchemaPojoGenerator.Configuration configuration = new JsonSchemaPojoGenerator.Configuration("src/test/resources/schema/simple", "target/generated-sources");
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(configuration);
        jsonSchemaPojoGenerator.generate();
        File expectedFile = new File("target/generated-sources/com/example/schemas/AddressV1.java");
        assertThat(expectedFile).exists().isNotEmpty();
        JavaClassSource javaClassSource = Roaster.parse(JavaClassSource.class, expectedFile);
        assertThat(javaClassSource.getPackage()).isEqualTo("com.example.schemas");
        assertThat(javaClassSource.getField("streetAddress")).isNotNull();
        assertThat(javaClassSource.getField("city")).isNotNull();
        assertThat(javaClassSource.getField("state")).isNotNull();
        assertThat(javaClassSource.getField("zipCode")).isNotNull();
    }

    @Test
    void parent_child() throws IOException {
        JsonSchemaPojoGenerator.Configuration configuration = new JsonSchemaPojoGenerator.Configuration("src/test/resources/schema/parent-child", "target/generated-sources");
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(configuration);
        jsonSchemaPojoGenerator.generate();
        File expectedFile = new File("target/generated-sources/com/example/schemas/ChildV1.java");
        assertThat(expectedFile).exists().isNotEmpty();
        JavaClassSource javaClassSource = Roaster.parse(JavaClassSource.class, expectedFile);
        assertThat(javaClassSource.getPackage()).isEqualTo("com.example.schemas");
        assertThat(javaClassSource.getField("firstName")).isNotNull();
        assertThat(javaClassSource.getField("lastName")).isNotNull();
        assertThat(javaClassSource.getField("age")).isNotNull();
    }

}

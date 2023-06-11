package io.schematools.json.generate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.schematools.json.JsonSchemaPojoGenerator;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorBasicTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema jsonSchema = factory.getSchema(JsonSchemaPojoGeneratorBasicTest.class.getResourceAsStream("/schema/basic/basic.schema.json"));
        JsonNode jsonNode = objectMapper.readTree(JsonSchemaPojoGeneratorBasicTest.class.getResourceAsStream("/schema/basic/basic.json"));
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        assertThat(errors).isEmpty();

    }

}

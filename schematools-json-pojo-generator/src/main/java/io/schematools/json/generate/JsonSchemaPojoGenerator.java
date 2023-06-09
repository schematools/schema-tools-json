package io.schematools.json.generate;

import io.schematools.json.IdAdapter;
import io.schematools.json.JsonSchemaPojoGeneratorConfiguration;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public class JsonSchemaPojoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaPojoGenerator.class);

    private final JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration;
    private final JsonSchemaLoader jsonSchemaLoader = new JsonSchemaLoader();

    public JsonSchemaPojoGenerator(JsonSchemaPojoGeneratorConfiguration jsonSchemaPojoGeneratorConfiguration) {
        this.jsonSchemaPojoGeneratorConfiguration = jsonSchemaPojoGeneratorConfiguration;
    }

    public void generate() {
        Map<IdAdapter, JsonSchema> jsonSchemaMap = jsonSchemaLoader.load(jsonSchemaPojoGeneratorConfiguration.sourcePath(), jsonSchemaPojoGeneratorConfiguration.targetPath());
        jsonSchemaMap.entrySet().stream()
                .forEach(entry -> {
                    entry.getValue().process();
                    entry.getValue().write();
                });
    }

}

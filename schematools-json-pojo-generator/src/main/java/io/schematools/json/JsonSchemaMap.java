package io.schematools.json;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JsonSchemaMap {

    private final Map<Id, JsonSchema> jsonSchemaMap;

    public JsonSchemaMap(Map<Id, JsonSchema> jsonSchemaMap) {
        this.jsonSchemaMap = jsonSchemaMap;
    }

    public Optional<JsonSchema> get(Id id) {
        return jsonSchemaMap.containsKey(id) ? Optional.of(jsonSchemaMap.get(id)) : Optional.empty();
    }

    public Stream<Map.Entry<Id, JsonSchema>> getStream() {
        return jsonSchemaMap.entrySet().stream();
    }

}

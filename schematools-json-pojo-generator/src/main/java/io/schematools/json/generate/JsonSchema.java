package io.schematools.json.generate;

import com.fasterxml.jackson.databind.JsonNode;
import io.schematools.json.IdAdapter;
import jakarta.annotation.Generated;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public record JsonSchema(Path path, JsonNode rootNode, IdAdapter idAdapter,
                         JavaClassSource javaClassSource, Map<IdAdapter, JsonSchema> jsonSchemaMap,
                         String targetPath) {

    public static JsonSchema create(Path path, JsonNode jsonNode, IdAdapter idAdapter, Map<IdAdapter, JsonSchema> jsonSchemaMap, String targetPath) {
        JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
        javaClass.setPackage("com.company.example").setName("Person");
        javaClass.addAnnotation(Generated.class).setStringValue("value", "io.schematools");
        JsonSchema jsonSchema = new JsonSchema(path, jsonNode, idAdapter, javaClass, jsonSchemaMap, targetPath);
        jsonSchemaMap.put(idAdapter, jsonSchema);
        return jsonSchema;
    }

    public void process() {

    }

    public void write() {
        try {
            String outputPath = targetPath + "/" + this.idAdapter.packageName().replace('.', '/');
            File file = new File(outputPath, this.javaSourceFileName());
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(javaClassSource.toString());
            writer.close();
            System.out.println("Java source file generated successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("An error occurred while generating the Java source file: " + e.getMessage());
        }
    }

    public String className() {
        return idAdapter.className() + idAdapter.version().toUpperCase();
    }

    public String javaSourceFileName() {
        return this.className() + ".java";
    }

}

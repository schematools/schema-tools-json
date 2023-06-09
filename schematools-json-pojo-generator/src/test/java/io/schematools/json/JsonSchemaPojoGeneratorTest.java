package io.schematools.json;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import jakarta.annotation.Generated;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorTest {

    @Test
    void simple() throws IOException {
        String expectedFilePath = "target/generated-sources/com/example/schemas/AddressV1.java";
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(new JsonSchemaPojoGeneratorConfiguration("src/test/resources/schema/simple", "target/generated-sources"));
        jsonSchemaPojoGenerator.generate();
        assertThat(new File(expectedFilePath)).exists();
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File(expectedFilePath));
        Name name = compilationUnit.getPackageDeclaration().get().getName();
        assertThat(name.toString()).isEqualTo("com.example.schemas");
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.getClassByName("AddressV1").get();
        assertThat(classOrInterfaceDeclaration.getAnnotationByClass(Generated.class)).isNotEmpty();
        assertThat(classOrInterfaceDeclaration.getFieldByName("streetAddress")).isNotEmpty();
        assertThat(classOrInterfaceDeclaration.getFieldByName("city")).isNotEmpty();
        assertThat(classOrInterfaceDeclaration.getFieldByName("state")).isNotEmpty();
        assertThat(classOrInterfaceDeclaration.getFieldByName("zipCode")).isNotEmpty();
    }

}

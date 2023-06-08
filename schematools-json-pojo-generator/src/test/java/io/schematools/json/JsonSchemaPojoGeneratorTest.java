package io.schematools.json;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaPojoGeneratorTest {

    @Test
    void simple() throws IOException {
        JsonSchemaPojoGenerator jsonSchemaPojoGenerator = new JsonSchemaPojoGenerator(new JsonSchemaPojoGeneratorConfiguration("src/test/resources/schema/simple", "target/generated-sources"));
        jsonSchemaPojoGenerator.generate();
        assertThat(new File("target/generated-sources/com/example/schemas/AddressV1.java")).exists();
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File("target/generated-sources/com/example/schemas/AddressV1.java"));
        Name name = compilationUnit.getPackageDeclaration().get().getName();
        assertThat(name.toString()).isEqualTo("com.example.schemas");
    }

}

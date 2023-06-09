package io.schematools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import jakarta.annotation.Generated;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public record JsonSchema(Path path, JsonNode rootNode, IdAdapter idAdapter,
                         CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {

    public static JsonSchema create(Path path, JsonNode jsonNode, IdAdapter idAdapter) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(idAdapter.packageName());
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.addClass(idAdapter.className());
        classOrInterfaceDeclaration.addAndGetAnnotation(Generated.class)
                .addPair("value", new StringLiteralExpr("io.schematools"));
        return new JsonSchema(path, jsonNode, idAdapter, compilationUnit, classOrInterfaceDeclaration);
    }

    public void process() {

    }

    public void write(String targetPath) {
        try {
            String outputPath = targetPath + "/" + this.idAdapter.packageName().replace('.', '/');
            File file = new File(outputPath, this.javaSourceFileName());
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(compilationUnit.toString());
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

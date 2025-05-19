package org.otherband.automigrate;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.otherband.automigrate.Commons.readResource;

public class ClassRenamerTest {

    private final ClassRenamer classRenamer = new ClassRenamer();

    @Test
    void renameWriterToAuthor() throws IOException {
        String starting = readResource("pre/uses_class.java-sample");
        String expectedResult = readResource("post/uses_class.java-sample");
        String result = classRenamer.renameClass(starting, new SymbolInformation(
                "com.github.writer",
                "Writer"
        ), new SymbolInformation(
                "com.github.writer",
                "Author"
        ));
        assertEquals(expectedResult, result);
    }

    @Test
    void doNotRenameUserClasses() throws IOException {
        String starting = readResource("pre/user_class_uses_class.java-sample");
        String expectedResult = readResource("post/user_class_uses_class.java-sample");

        String result = classRenamer.renameClass(starting,
                new SymbolInformation("com.github.javaparser", "JavaParser"),
                new SymbolInformation("com.github.javaparser", "EfficientJavaParser"));

        assertEquals(expectedResult, result);
    }

}

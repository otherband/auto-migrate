package org.otherband.automigrate;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.otherband.automigrate.Commons.readResource;

public class ClassRenamerTest {

    @Test
    void renameWriterToAuthor() throws IOException {
        ClassRenamer classRenamer = new ClassRenamer();
        String starting = readResource("pre/uses_class.java-sample");
        String expectedResult = readResource("post/uses_class.java-sample");
        String result = classRenamer.renameClass(starting, "Writer", "Author");
        assertEquals(expectedResult, result);
    }
}

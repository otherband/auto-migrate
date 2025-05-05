package org.otherband;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.otherband.Commons.readResource;

class MethodRenamerTest {

    private MethodRenamer rewriter;

    @BeforeEach
    void setup() {
        rewriter = new MethodRenamer();
    }

    @Test
    void renames() throws IOException {
        String starting = readResource("pre/uses_functions.java-sample");
        String expected = readResource("post/uses_functions.java-sample");
        assertEquals(expected, rewriter.renameMethod(starting, "java.util.List", "of", "ofElements"));
    }

    @Test
    void renamesOnlyDesiredMethod() throws IOException {
        String starting = readResource("pre/uses_functions_with_same_name.java-sample");
        String expected = readResource("post/uses_functions_with_same_name.java-sample");
        assertEquals(expected, rewriter.renameMethod(starting,
                        "java.util.List",
                        "of",
                        "ofElements"));
    }

    @Test
    void noChangeToFileWithNoMethodCalls() throws IOException {
        String starting = readResource("pre/no_functions.java-sample");
        assertEquals(starting, rewriter.renameMethod(starting,
                        "java.util.List",
                        "of",
                        "ofElements"));
    }

}
package org.otherband;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEqualsIgnoringWhitespace(expected,
                rewriter.renameMethod(starting, "of", "ofElements"));
    }

    private void assertEqualsIgnoringWhitespace(String expected, String result) {
        assertEquals(removeWhitespace(expected), removeWhitespace(result));
    }

    private String removeWhitespace(String string) {
        return string.replaceAll("\\s+", "");
    }

    private static String readResource(String resource) throws IOException {
        return Files.readString(Path.of(getResource(resource).getPath()));
    }

    private static URL getResource(String resource) {
        return Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(resource));
    }

}
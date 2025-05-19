package org.otherband.automigrate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.otherband.automigrate.Commons.readResource;

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
        assertEquals(expected, rewriter.renameMethod(starting,
                new SymbolInformation("java.util.List", "of"),
                new SymbolInformation("java.util.List", "ofElements")
        ));
    }
    @Test
    void renamesFullyQualifiedMethods() throws IOException {
        String starting = readResource("pre/uses_fully_qualified_method.java-sample");
        String expected = readResource("post/uses_fully_qualified_method.java-sample");
        assertEquals(expected, rewriter.renameMethod(starting,
                new SymbolInformation("java.util.List", "of"),
                new SymbolInformation("java.util.List", "ofElements")
        ));
    }

    @Test
    void renamesWithWildCardImports() throws IOException {
        String starting = readResource("pre/uses_wildcard_import.java-sample");
        String expected = readResource("post/uses_wildcard_import.java-sample");
        assertEquals(expected, rewriter.renameMethod(starting,
                new SymbolInformation("java.util.List", "of"),
                new SymbolInformation("java.util.List", "ofElements")
        ));
    }

    @Test
    void renamesOnlyDesiredMethod() throws IOException {
        String starting = readResource("pre/uses_functions_with_same_name.java-sample");
        String expected = readResource("post/uses_functions_with_same_name.java-sample");
        assertEquals(expected, rewriter.renameMethod(starting,
                new SymbolInformation("java.util.List", "of"),
                new SymbolInformation("java.util.List", "ofElements")
        ));
    }

    @Test
    void noChangeToFileWithNoMethodCalls() throws IOException {
        String starting = readResource("pre/no_functions.java-sample");
        assertEquals(starting, rewriter.renameMethod(starting,
                new SymbolInformation("java.util.List", "of"),
                new SymbolInformation("java.util.List", "ofElements")
        ));
    }

}
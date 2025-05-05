package org.otherband;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class ClassRenamer {
    public String renameClass(String oldFile,
                              String scope,
                              String oldName,
                              String newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(LexicalPreservingPrinter::setup)
                .map(compilationUnit -> renameClass(scope, oldName, newName, compilationUnit))
                .map(LexicalPreservingPrinter::print)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse java file [%s]".formatted(oldFile)));
    }

    private static CompilationUnit renameClass(String scope, String oldName, String newName, CompilationUnit compilationUnit) {
        TypeRenamer typeRenamer = new TypeRenamer(oldName, newName);
        compilationUnit.accept(typeRenamer, null);
        return compilationUnit;
    }


    private static class TypeRenamer extends VoidVisitorAdapter<Void> {
        private final String oldName;
        private final String newName;

        private TypeRenamer(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }


        public void visit(ClassOrInterfaceType classType, Void arg) {
            super.visit(classType, arg);
            if (doesNameMatch(classType)) {
                classType.setName(newName);
            }
        }

        private boolean doesNameMatch(NodeWithSimpleName<?> type) {
            return oldName.equals(type.getNameAsString());
        }
    }

}

package org.otherband.automigrate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class ClassRenamer {
    public String renameClass(String oldFile,
                              String oldName,
                              String newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(LexicalPreservingPrinter::setup)
                /*
                Must be done in two steps. Does not work otherwise, does not work in the opposite order.
                 */
                .map(compilationUnit -> renameVariableDeclarations(oldName, newName, compilationUnit))
                .map(compilationUnit -> renameClass(oldName, newName, compilationUnit))
                .map(LexicalPreservingPrinter::print)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse java file [%s]".formatted(oldFile)));
    }

    private static CompilationUnit renameClass(String oldName, String newName, CompilationUnit compilationUnit) {
        TypeRenamer typeRenamer = new TypeRenamer(oldName, newName);
        compilationUnit.accept(typeRenamer, null);
        return compilationUnit;
    }

    private static CompilationUnit renameVariableDeclarations(String oldName, String newName, CompilationUnit compilationUnit) {
        VariableDeclarationRenamer variableDeclarationRenamer = new VariableDeclarationRenamer(oldName, newName);
        compilationUnit.accept(variableDeclarationRenamer, null);
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

    private static class VariableDeclarationRenamer extends VoidVisitorAdapter<Void> {
        private final String oldName;
        private final String newName;

        private VariableDeclarationRenamer(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(VariableDeclarationExpr declarationExpr, Void arg) {
            super.visit(declarationExpr, arg);
            declarationExpr.getVariables().forEach(variableDeclarator -> {
                boolean shouldBeChanged = variableDeclarator.getType().isClassOrInterfaceType() && oldName.equals(variableDeclarator.getType().asClassOrInterfaceType().getNameAsString());
                if (shouldBeChanged) {
                    variableDeclarator.setType(newName);
                }
            });
        }

    }

}

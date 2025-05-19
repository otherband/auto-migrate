package org.otherband.automigrate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import static java.util.Optional.ofNullable;

public class ClassRenamer {
    public String renameClass(String oldFile,
                              String oldName,
                              String newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(LexicalPreservingPrinter::setup)
                /*
                Must be done in two steps. Does not work otherwise, does not work in the opposite order.
                Works in one step without the `LexicalPreservingPrinter`.
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

        @Override
        public void visit(ClassOrInterfaceType classType, Void arg) {
            super.visit(classType, arg);
            if (doesNameMatch(classType)) {
                classType.setName(newName);
            }
        }

        @Override
        public void visit(ImportDeclaration importDeclaration, Void arg) {
            super.visit(importDeclaration, arg);
            if (importDeclaration.getNameAsString().contains(oldName)) {
                importDeclaration.setName(importDeclaration.getNameAsString().replace(oldName, newName));
            }
        }

        private boolean doesNameMatch(ClassOrInterfaceType type) {
            return oldName.equals(type.getNameAsString()) ||
                    type.getScope()
                            .map(Node::toString)
                            .map(scopeName -> scopeName.concat(".").concat(type.getNameAsString()))
                            .map(oldName::equals)
                            .orElse(false);
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
            declarationExpr.getVariables().forEach(this::renameLeftHandSide);
        }

        private void renameLeftHandSide(VariableDeclarator variableDeclarator) {
            if (shouldBeChanged(variableDeclarator)) {
                variableDeclarator.setType(newName);
            }
        }

        private boolean shouldBeChanged(VariableDeclarator variableDeclarator) {
            if (variableDeclarator.getType().isClassOrInterfaceType()) {
                ClassOrInterfaceType classOrInterfaceType = variableDeclarator.getType().asClassOrInterfaceType();
                return oldName.equals(classOrInterfaceType.getNameAsString());
            }
            return false;
        }
    }

}

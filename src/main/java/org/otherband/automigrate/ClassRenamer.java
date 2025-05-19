package org.otherband.automigrate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Objects;

public class ClassRenamer {
    public String renameClass(String oldFile,
                              SymbolInformation oldName,
                              SymbolInformation newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        if (!oldName.qualifiedScopeName().equals(newName.qualifiedScopeName())) {
            throw new UnsupportedOperationException("Scope renaming has not been implemented for classes!");
        }
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

    private static CompilationUnit renameClass(SymbolInformation oldName, SymbolInformation newName, CompilationUnit compilationUnit) {
        TypeRenamer typeRenamer = new TypeRenamer(oldName, newName);
        compilationUnit.accept(typeRenamer, null);
        return compilationUnit;
    }

    private static CompilationUnit renameVariableDeclarations(SymbolInformation oldName, SymbolInformation newName, CompilationUnit compilationUnit) {
        VariableDeclarationRenamer variableDeclarationRenamer = new VariableDeclarationRenamer(oldName, newName);
        compilationUnit.accept(variableDeclarationRenamer, null);
        return compilationUnit;
    }

    private static class TypeRenamer extends VoidVisitorAdapter<Void> {
        private final SymbolInformation oldName;
        private final SymbolInformation newName;

        private TypeRenamer(SymbolInformation oldName, SymbolInformation newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(ClassOrInterfaceType classType, Void arg) {
            super.visit(classType, arg);
            if (doesNameMatch(classType)) {
                classType.setName(newName.simpleName());
            }
        }

        @Override
        public void visit(ImportDeclaration importDeclaration, Void arg) {
            super.visit(importDeclaration, arg);
            if (importDeclaration.getNameAsString().contains(oldName.simpleName())) {
                importDeclaration.setName(importDeclaration.getNameAsString().replace(oldName.simpleName(), newName.simpleName()));
            }
        }

        private boolean doesNameMatch(ClassOrInterfaceType type) {
            return oldName.simpleName().equals(type.getNameAsString()) ||
                    type.getScope()
                            .map(Node::toString)
                            .map(scopeName -> VariableDeclarationRenamer.toQualifiedName(scopeName, type.getNameAsString()))
                            .map(fqn -> fqn.equals(VariableDeclarationRenamer.toQualifiedName(oldName.qualifiedScopeName(), oldName.simpleName())))
                            .orElse(false);
        }
    }

    private static class VariableDeclarationRenamer extends VoidVisitorAdapter<Void> {
        private final SymbolInformation oldName;
        private final SymbolInformation newName;

        private VariableDeclarationRenamer(SymbolInformation oldName, SymbolInformation newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(VariableDeclarationExpr declarationExpr, Void arg) {
            super.visit(declarationExpr, arg);
            declarationExpr.getVariables().forEach(this::renameLeftHandSide);
        }

        private void renameLeftHandSide(VariableDeclarator variableDeclarator) {
            if (variableDeclarator.getType().isClassOrInterfaceType()) {
                ClassOrInterfaceType classOrInterfaceType = variableDeclarator.getType().asClassOrInterfaceType();
                if (doesFullyQualifiedNameMatch(classOrInterfaceType)) {
                    variableDeclarator.setType(toQualifiedName(newName.qualifiedScopeName(), newName.simpleName()));
                } else if (doesSimpleNameMatch(classOrInterfaceType)) {
                    variableDeclarator.setType(newName.simpleName());
                }
            }
        }

        private boolean doesSimpleNameMatch(ClassOrInterfaceType classOrInterfaceType) {
            return oldName.simpleName().equals(classOrInterfaceType.getNameAsString());
        }

        private Boolean doesFullyQualifiedNameMatch(ClassOrInterfaceType classOrInterfaceType) {
            return classOrInterfaceType.getScope()
                    .map(Objects::toString)
                    .map(scopeName -> toQualifiedName(scopeName, classOrInterfaceType.getNameAsString()))
                    .map(fqn -> fqn.equals(toQualifiedName(oldName.qualifiedScopeName(), oldName.simpleName())))
                    .orElse(false);
        }

        private static String toQualifiedName(String scopeName, String simpleName) {
            return scopeName.concat(".").concat(simpleName);
        }
    }

}

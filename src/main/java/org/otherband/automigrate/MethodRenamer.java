package org.otherband.automigrate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MethodRenamer {
    public String renameMethod(String oldFile,
                               SymbolInformation oldName,
                               SymbolInformation newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        final AtomicReference<Boolean> importExists = new AtomicReference<>(false);
        return parseResult.getResult()
                .map(LexicalPreservingPrinter::setup)
                .map(compilationUnit -> checkImport(oldName, compilationUnit))
                .map(importCheckResult -> renameMethod(importCheckResult, oldName, newName))
                .map(LexicalPreservingPrinter::print)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse java file [%s]".formatted(oldFile)));
    }

    private ImportCheckResult checkImport(SymbolInformation oldName,
                                          CompilationUnit compilationUnit) {
        AtomicBoolean importExists = new AtomicBoolean(false);
        AtomicBoolean wildCardImportExists = new AtomicBoolean(false);
        ImportChecker importChecker = new ImportChecker(oldName, importExists, wildCardImportExists);
        compilationUnit.accept(importChecker, null);
        return new ImportCheckResult(
                compilationUnit,
                wildCardImportExists.get(),
                importExists.get()
        );
    }

    private static CompilationUnit renameMethod(ImportCheckResult importCheckResult,
                                                SymbolInformation oldName,
                                                SymbolInformation newName) {
        MethodUseRenamer methodUseRenamer = new MethodUseRenamer(importCheckResult.importExists(),
                importCheckResult.wildCardImportExists(),
                oldName, newName);
        importCheckResult.compilationUnit.accept(methodUseRenamer, null);
        return importCheckResult.compilationUnit;
    }


    private static class MethodUseRenamer extends VoidVisitorAdapter<Void> {
        private final boolean importExists;
        private final boolean wildCardImportExists;
        private final SymbolInformation oldName;
        private final SymbolInformation newName;

        private MethodUseRenamer(boolean importExists, boolean wildCardImportExists, SymbolInformation oldName, SymbolInformation newName) {
            this.importExists = importExists;
            this.wildCardImportExists = wildCardImportExists;
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(ImportDeclaration n, Void arg) {
            super.visit(n, arg);
        }

        public void visit(MethodCallExpr methodCall, Void arg) {
            super.visit(methodCall, arg);
            if ((wildCardImportExists && methodCall.getScope().isEmpty()) || (importExists && doesScopeMatch(methodCall))) {
                if (oldName.simpleName().equals(methodCall.getNameAsString())) {
                    methodCall.setName(newName.simpleName());
                }
            } else {
                String fullyQualifiedName = oldName.qualifiedScopeName().concat(".").concat(oldName.simpleName());
                String currentCallFullyQualifiedName = methodCall.getScope().map(Objects::toString).orElse("").concat(".").concat(methodCall.getNameAsString());
                if (fullyQualifiedName.equals(currentCallFullyQualifiedName)) {
                    methodCall.setName(newName.simpleName());
                }
            }
        }

        private Boolean doesScopeMatch(MethodCallExpr methodCall) {
            return methodCall.getScope()
                    .map(scope -> {
                        if (scope.isNameExpr()) {
                            return scope.asNameExpr().getNameAsString();
                        }
                        return "---"; // cannot be contained
                    })
                    .map(scopeName -> oldName.qualifiedScopeName().contains(scopeName))
                    .orElse(false);
        }

    }

    private static class ImportChecker extends VoidVisitorAdapter<Void> {
        private final SymbolInformation oldName;
        private final AtomicBoolean importExists;
        private final AtomicBoolean wildCardImportExists;

        private ImportChecker(SymbolInformation oldName, AtomicBoolean importExists, AtomicBoolean wildCardImportExists) {
            this.oldName = oldName;
            this.importExists = importExists;
            this.wildCardImportExists = wildCardImportExists;
        }

        @Override
        public void visit(ImportDeclaration importDeclaration, Void arg) {
            super.visit(importDeclaration, arg);

            if (oldName.qualifiedScopeName().contains(importDeclaration.getNameAsString())) {
                importExists.set(true);
                if (importDeclaration.isAsterisk()) {
                    wildCardImportExists.set(true);
                }
            }
        }
    }

    private record ImportCheckResult(CompilationUnit compilationUnit,
                                     boolean wildCardImportExists,
                                     boolean importExists) {

    }

}

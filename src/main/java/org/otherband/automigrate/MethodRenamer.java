package org.otherband.automigrate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class MethodRenamer {
    public String renameMethod(String oldFile,
                               String scope,
                               String oldName,
                               String newName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(LexicalPreservingPrinter::setup)
                .map(compilationUnit -> renameMethod(scope, oldName, newName, compilationUnit))
                .map(LexicalPreservingPrinter::print)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse java file [%s]".formatted(oldFile)));
    }

    private static CompilationUnit renameMethod(String scope, String oldName, String newName, CompilationUnit compilationUnit) {
        MethodUseRenamer methodUseRenamer = new MethodUseRenamer(scope, oldName, newName);
        compilationUnit.accept(methodUseRenamer, null);
        return compilationUnit;
    }


    private static class MethodUseRenamer extends VoidVisitorAdapter<Void> {
        private final String owningClass;
        private final String oldName;
        private final String newName;

        private MethodUseRenamer(String owningClass, String oldName, String newName) {
            this.owningClass = owningClass;
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(ClassOrInterfaceType n, Void arg) {
            super.visit(n, arg);
        }

        public void visit(MethodCallExpr methodCall, Void arg) {
            super.visit(methodCall, arg);
            if (doesScopeMatch(methodCall) && doesNameMatch(methodCall)) {
                methodCall.setName(newName);
            }
        }

        private boolean doesNameMatch(MethodCallExpr methodCall) {
            return oldName.equals(methodCall.getNameAsString());
        }

        private boolean doesScopeMatch(MethodCallExpr methodCall) {
            return methodCall.getScope()
                    .map(this::doesScopeMatch)
                    .orElse(false);
        }

        private boolean doesScopeMatch(Expression expression) {
            return owningClass.contains(expression.asNameExpr().getName().asString());
        }
    }

}

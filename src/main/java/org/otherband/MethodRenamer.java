package org.otherband;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.function.Function;

public class MethodRenamer {
    public String renameMethod(String oldFile,
                               String scope,
                               String oldMethodName,
                               String newMethodName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(compilationUnit -> {
                    MethodUseRenamer methodUseRenamer = new MethodUseRenamer(scope, oldMethodName, newMethodName);
                    compilationUnit.accept(methodUseRenamer, null);
                    return compilationUnit.toString();
                })
                .orElse("");
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

        public void visit(MethodCallExpr methodCall, Void arg) {
            super.visit(methodCall, arg);
            boolean matchingScope = methodCall.getScope()
                    .map(this::doesScopeMatch)
                    .orElse(false);
            if (matchingScope && methodCall.getNameAsString().equals(oldName)) {
                methodCall.setName(newName);
            }
        }

        private boolean doesScopeMatch(Expression expression) {
            return owningClass.contains(expression.asNameExpr().getName().asString());
        }
    }

    private static Function<Expression, SimpleName> getNameFunction() {
        return expression -> expression.asNameExpr().getName();
    }
}

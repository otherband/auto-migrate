package org.otherband;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodRenamer {
    public String renameMethod(String oldFile, String oldMethodName, String newMethodName) {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(oldFile);
        return parseResult.getResult()
                .map(compilationUnit -> {
                    MethodUseRenamer methodUseRenamer = new MethodUseRenamer(oldMethodName, newMethodName);
                    compilationUnit.accept(methodUseRenamer, null);
                    return compilationUnit.toString();
                })
                .orElse("");
    }


    private static class MethodUseRenamer extends VoidVisitorAdapter<Void> {
        private final String oldName;
        private final String newName;

        private MethodUseRenamer(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        public void visit(MethodCallExpr methodCall, Void arg) {
            super.visit(methodCall, arg);
            if (methodCall.getNameAsString().equals(oldName)) {
                methodCall.setName(newName);
            }
        }
    }
}

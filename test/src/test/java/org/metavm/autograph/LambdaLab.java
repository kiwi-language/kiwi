package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiLambdaExpression;

public class LambdaLab {

    public static void main(String[] args) {
        var foo = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.AstLambdaFoo");
        foo.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitLambdaExpression(PsiLambdaExpression expression) {
            super.visitLambdaExpression(expression);
        }
    }

}

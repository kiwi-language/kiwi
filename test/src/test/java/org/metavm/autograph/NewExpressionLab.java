package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiNewExpression;

public class NewExpressionLab {

    public static void main(String[] args) {
        var foo = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.NewArrayFoo");
        foo.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            System.out.println(expression.getText());
        }
    }

}

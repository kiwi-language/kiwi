package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiNewExpression;
import tech.metavm.autograph.mocks.NewArrayFoo;

public class NewExpressionLab {

    public static void main(String[] args) {
        var foo = TranspileTestTools.getPsiClass(NewArrayFoo.class);
        foo.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            System.out.println(expression.getText());
        }
    }

}

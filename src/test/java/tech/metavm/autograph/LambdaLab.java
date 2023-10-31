package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiLambdaExpression;
import tech.metavm.autograph.mocks.AstLambdaFoo;

public class LambdaLab {

    public static void main(String[] args) {
        var foo = TranspileTestTools.getPsiClass(AstLambdaFoo.class);
        foo.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitLambdaExpression(PsiLambdaExpression expression) {
            super.visitLambdaExpression(expression);
        }
    }

}

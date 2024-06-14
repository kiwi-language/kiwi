package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiNewExpression;
import org.metavm.autograph.mocks.ConstructorFoo;

public class ConstructorLab {

    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFile(ConstructorFoo.class);
        file.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            var constructor = expression.resolveConstructor();
            var type = expression.getType();
            var classRef = expression.getClassReference();

            super.visitNewExpression(expression);
        }
    }

}

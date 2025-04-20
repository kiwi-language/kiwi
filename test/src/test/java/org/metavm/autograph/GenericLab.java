package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

import java.util.Objects;

public class GenericLab {

    public static void main(String[] args) {
        var foo = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.GenericFoo");
        foo.accept(new Visitor());
    }

    public static class  Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            var qualifierExpr = expression.getMethodExpression().getQualifierExpression();
            var methodGenerics = expression.resolveMethodGenerics();
            var substitutor = methodGenerics.getSubstitutor();
            var method = (PsiMethod) methodGenerics.getElement();
            var paramType = Objects.requireNonNull(Objects.requireNonNull(method).getParameterList()
                    .getParameter(0)).getType();
            System.out.println(substitutor.substitute(paramType));
        }
    }

}

package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;

import java.util.Objects;

public class AstWildcardLab {

    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.AstWildcardFoo");
        file.accept(new Visitor());
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethod(PsiMethod method) {
            var returnType = (PsiClassType) Objects.requireNonNull(method.getReturnType());
            var typeArgs = returnType.getParameters();
            super.visitMethod(method);
        }
    }

}

package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import tech.metavm.autograph.mocks.TypeFoo;

public class TypeLab {

    public static void main(String[] args) {
        var psiFile = TranspileTestTools.getPsiJavaFile(TypeFoo.class);
        psiFile.accept(new TypeLabVisitor());
    }

    private static class TypeLabVisitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethod(PsiMethod method) {
            var paramList = method.getParameterList();
            var param = paramList.getParameters()[0];
            PsiClassReferenceType type = (PsiClassReferenceType) param.getType();
            System.out.println(type);
        }
    }

}

package tech.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;

public class LabVisitor extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        if(aClass.isRecord()) {
            Arrays.stream(aClass.getFields()).forEach(this::visitField);
            Arrays.stream(aClass.getMethods()).forEach(this::visitMethod);
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
    }

    @Override
    public void visitField(PsiField field) {
        super.visitField(field);
    }
}

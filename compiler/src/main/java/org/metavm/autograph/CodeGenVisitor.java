package org.metavm.autograph;

import com.intellij.psi.PsiClass;

import java.util.Arrays;

public class CodeGenVisitor extends VisitorBase {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        if(psiClass.isRecord()) {
            Arrays.stream(psiClass.getFields()).forEach(this::visitField);
            Arrays.stream(psiClass.getMethods()).forEach(this::visitMethod);
        }
    }
}

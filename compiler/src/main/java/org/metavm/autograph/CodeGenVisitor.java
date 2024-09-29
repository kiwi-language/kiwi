package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
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

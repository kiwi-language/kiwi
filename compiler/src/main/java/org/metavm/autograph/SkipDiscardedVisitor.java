package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SkipDiscardedVisitor extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        if(!TranspileUtils.isDiscarded(aClass))
            super.visitClass(aClass);
    }
}

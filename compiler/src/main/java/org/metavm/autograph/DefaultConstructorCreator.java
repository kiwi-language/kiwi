package org.metavm.autograph;

import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeParameter;
import org.metavm.util.Utils;

import java.util.List;

public class DefaultConstructorCreator extends VisitorBase {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        if(psiClass instanceof PsiTypeParameter || psiClass.isInterface() || psiClass instanceof PsiAnonymousClass)
            return;
        if (!Utils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor)) {
            psiClass.addBefore(TranspileUtils.createConstructor(psiClass.getName(), !psiClass.isEnum()), null);
        }
    }
}

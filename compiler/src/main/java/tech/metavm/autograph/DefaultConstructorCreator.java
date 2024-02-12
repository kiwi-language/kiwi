package tech.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiTypeParameter;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class DefaultConstructorCreator extends VisitorBase {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        if(psiClass instanceof PsiTypeParameter || psiClass.isInterface() ||
                Objects.requireNonNull(psiClass.getModifierList()).hasModifierProperty(PsiModifier.ABSTRACT))
            return;
        boolean hashConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
        if (!psiClass.isInterface() && !hashConstructor) {
            psiClass.addBefore(TranspileUtil.createConstructor(psiClass.getName(), !psiClass.isEnum()), null);
        }
    }
}

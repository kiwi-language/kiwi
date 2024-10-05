package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.IdentitySet;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class InnerClassCopier extends VisitorBase {

    private final Set<PsiClass> copies = new IdentitySet<>();

    @Override
    public void visitClass(PsiClass psiClass) {
        if (copies.contains(psiClass))
            return;
        if (TranspileUtils.isNonStaticInnerClass(psiClass)) {
            var copy = (PsiClass) psiClass.copy();
            copy.setName(psiClass.getName() + "$");
            copy = (PsiClass) Objects.requireNonNull(psiClass.getContainingClass()).addAfter(copy, psiClass);
            copies.add(copy);
            copy.putUserData(Keys.ORIGINAL_NAME, psiClass.getName());
            copy.putUserData(Keys.INNER_CLASS_COPY, true);
            copy.accept(new ClassRefSubstitutor(psiClass, copy));
            recordSubstitution(psiClass, copy);
            psiClass.putUserData(Keys.DISCARDED, true);
        } else
            super.visitClass(psiClass);
    }

    private static class ClassRefSubstitutor extends VisitorBase {

        private final PsiClass sourceClass;
        private final PsiClass targetClass;

        private ClassRefSubstitutor(PsiClass sourceClass, PsiClass targetClass) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
        }

        @Override
        public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
            if (reference.resolve() instanceof PsiClass k && k == sourceClass)
                replace(
                        Objects.requireNonNull(reference.getReferenceNameElement()),
                        TranspileUtils.createIdentifier(targetClass.getName())
                );
        }
    }

    private void recordSubstitution(PsiClass substituted, PsiClass substitution) {
        substituted.putUserData(Keys.SUBSTITUTION, substitution);
        for (int i = 0; i < substituted.getInnerClasses().length; i++) {
            recordSubstitution(substituted.getInnerClasses()[i], substitution.getInnerClasses()[i]);
        }
    }

}

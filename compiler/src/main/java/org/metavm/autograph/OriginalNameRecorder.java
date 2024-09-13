package org.metavm.autograph;

import com.intellij.psi.*;

import java.util.Objects;

public class OriginalNameRecorder extends VisitorBase {

    @Override
    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
        super.visitReferenceElement(reference);
        if(reference.resolve() instanceof PsiClass klass) {
            var origName = klass.getUserData(Keys.ORIGINAL_NAME);
            if(origName != null) {
                Objects.requireNonNull(reference.getReferenceNameElement()).putUserData(Keys.ORIGINAL_NAME, origName);
            }
        }
    }
}

package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiIdentifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InnerClassTransformFinalizer extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        if(TranspileUtils.isDiscarded(aClass))
            aClass.delete();
        else {
            super.visitClass(aClass);
            var originalName = aClass.getUserData(Keys.ORIGINAL_NAME);
            if (originalName != null)
                aClass.setName(originalName);
        }
    }

    @Override
    public void visitIdentifier(PsiIdentifier identifier) {
        var origName = identifier.getUserData(Keys.ORIGINAL_NAME);
        if (origName != null) {
            replace(identifier, TranspileUtils.createIdentifier(origName));
        }
    }


}

package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;

public class OrdinalRecorder extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        if (aClass.isEnum()) {
            int ordinal = 0;
            for (PsiField field : aClass.getFields()) {
                if (field instanceof PsiEnumConstant)
                    field.putUserData(Keys.ORDINAL, ordinal++);
            }
            aClass.putUserData(Keys.ENUM_CONSTANT_COUNT, ordinal);
        }
    }

}

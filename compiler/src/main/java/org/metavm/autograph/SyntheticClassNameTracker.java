package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

import java.util.regex.Pattern;

public class SyntheticClassNameTracker extends VisitorBase {

    public static final Pattern PTN = Pattern.compile("\\$(0|([1-9][0-9]*))");

    private int nextSeq = 0;

    @Override
    public void visitJavaFile(PsiJavaFile file) {
        super.visitJavaFile(file);
        file.putUserData(Keys.MAX_SYNTHETIC_CLASS_SEQ, nextSeq);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if(aClass.getName() != null) {
            var m = PTN.matcher(aClass.getName());
            if(m.matches()) {
                var seq = Integer.parseInt(m.group(1));
                nextSeq = Math.max(nextSeq, seq + 1);
            }
        }
        super.visitClass(aClass);
    }



}

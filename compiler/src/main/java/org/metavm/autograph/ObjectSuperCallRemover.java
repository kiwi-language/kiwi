package org.metavm.autograph;

import com.intellij.psi.PsiExpressionStatement;

public class ObjectSuperCallRemover extends VisitorBase {

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        if(TranspileUtils.isObjectSuperCall(statement)) {
            replace(statement, TranspileUtils.createComment("// Removed object super call"));
        }
    }
}

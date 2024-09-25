package org.metavm.autograph;

import com.intellij.psi.*;

public class BodyNormalizer extends VisitorBase {

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitIfStatement(statement);
        if(statement.getThenBranch() != null)
            normalizeBody(statement.getThenBranch());
        if(statement.getElseBranch() != null)
            normalizeBody(statement.getElseBranch());
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        super.visitWhileStatement(statement);
        normalizeBody(statement.getBody());
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        super.visitForeachStatement(statement);
        normalizeBody(statement.getBody());
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitForStatement(statement);
        normalizeBody(statement.getBody());
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        super.visitDoWhileStatement(statement);
        normalizeBody(statement.getBody());
    }

    private void normalizeBody(PsiStatement body) {
        if(!(body instanceof PsiBlockStatement)) {
            replace(body, TranspileUtils.createBlockStatement(body));
        }
    }

}

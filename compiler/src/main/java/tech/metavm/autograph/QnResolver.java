package tech.metavm.autograph;

import com.intellij.psi.*;

public class QnResolver extends JavaRecursiveElementVisitor {

    private void processNameElement(PsiElement element) {
        QnFactory.getOrCreateQn(element);
        super.visitElement(element);
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitThisExpression(PsiThisExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitArrayAccessExpression(PsiArrayAccessExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitVariable(PsiVariable variable) {
        processNameElement(variable);
    }

}

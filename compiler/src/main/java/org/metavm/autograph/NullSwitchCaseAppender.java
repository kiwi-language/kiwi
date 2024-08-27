package org.metavm.autograph;

import com.intellij.psi.*;

import java.util.Objects;

public class NullSwitchCaseAppender extends VisitorBase {

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        super.visitSwitchExpression(expression);
        processSwitch(expression);
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        super.visitSwitchStatement(statement);
        processSwitch(statement);
    }

    private boolean isNullCaseCovered(PsiSwitchBlock switchBlock) {
        for (PsiStatement statement : Objects.requireNonNull(switchBlock.getBody()).getStatements()) {
            if(statement instanceof PsiSwitchLabeledRuleStatement labeledRuleStmt) {
                if(labeledRuleStmt.isDefaultCase())
                    return true;
                for (PsiCaseLabelElement element : Objects.requireNonNull(labeledRuleStmt.getCaseLabelElementList()).getElements()) {
                    if(element instanceof PsiLiteralExpression literalExpr && literalExpr.getValue() == null)
                        return true;
                }
            }
        }
        return false;
    }

    private void processSwitch(PsiSwitchBlock statement) {
        if(TranspileUtils.isEnum(Objects.requireNonNull(statement.getExpression()).getType()) && !isNullCaseCovered(statement)) {
            Objects.requireNonNull(statement.getBody()).addBefore(
                    TranspileUtils.createStatementFromText("case null -> throw new NullPointerException();"),
                    null
            );
        }
    }

}

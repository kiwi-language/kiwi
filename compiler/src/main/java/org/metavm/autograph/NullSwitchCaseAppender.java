package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static org.metavm.autograph.TranspileUtils.createStatementFromText;

@Slf4j
public class NullSwitchCaseAppender extends SkipDiscardedVisitor {

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
            if(statement instanceof PsiSwitchLabelStatement labeledRuleStmt) {
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
            var body = Objects.requireNonNull(statement.getBody());
            body.addBefore(createStatementFromText("case null:"), null);
            body.addBefore(createStatementFromText("throw new NullPointerException();"), null);
        }
    }

}

package org.metavm.autograph;

import com.intellij.psi.*;

import static java.util.Objects.requireNonNull;

public class EnhancedSwitchTransformer extends VisitorBase {

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        super.visitSwitchExpression(expression);
        processSwitch(expression, true);
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        super.visitSwitchStatement(statement);
        processSwitch(statement, false);
    }

    private void processSwitch(PsiSwitchBlock switchBlock, boolean isSwitchExpression) {
        var body = requireNonNull(switchBlock.getBody());
        for (PsiStatement statement : body.getStatements()) {
            if (statement instanceof PsiSwitchLabeledRuleStatement labeledRule) {
                var text = labeledRule.isDefaultCase() ? "default:" :
                        "case " + requireNonNull(labeledRule.getCaseLabelElementList()).getText() + ":";
                var replacement = labeledRule.replace(TranspileUtils.createStatementFromText(text));
                var caseBody = requireNonNull(labeledRule.getBody());
                if (caseBody instanceof PsiExpressionStatement && isSwitchExpression)
                    body.addAfter(TranspileUtils.createStatementFromText("yield " + caseBody.getText()), replacement);
                else {
                    var caseBodyReplacement = (PsiStatement) body.addAfter(caseBody.copy(), replacement);
                    if (!isSwitchExpression)
                        body.addAfter(TranspileUtils.createStatementFromText("break;"), caseBodyReplacement);
                }
            }
        }
    }

}

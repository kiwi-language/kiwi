package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.util.NncUtils;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtil.createStatementFromText;

public class SwitchLabelStatementTransformer extends VisitorBase {

    @Override
    public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
        super.visitSwitchLabelStatement(statement);
    }

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        super.visitSwitchExpression(expression);
        if(TranspileUtil.isColonSwitch(expression)) {
            processSwitchBody(requireNonNull(expression.getBody()));
        }
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        super.visitSwitchStatement(statement);
        if (TranspileUtil.isColonSwitch(statement)) {
            processSwitchBody(requireNonNull(statement.getBody()));
        }
    }

    private void processSwitchBody(PsiCodeBlock body) {
        var stmts = NncUtils.requireNonNull(body).getStatements();
        var newBody = TranspileUtil.createCodeBlock();
        PsiCodeBlock dest = null;
        for (PsiStatement stmt : stmts) {
            if (stmt instanceof PsiSwitchLabelStatement labelStmt) {
                String text;
                if (labelStmt.isDefaultCase()) {
                    text = "default -> {}";
                } else {
                    text = "case " + requireNonNull(labelStmt.getCaseLabelElementList()).getText() + " -> {}";
                }
                var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) newBody.add(createStatementFromText(text));
                dest = ((PsiBlockStatement) requireNonNull(labeledRuleStmt.getBody())).getCodeBlock();
            } else if (!isRedundantBreak(stmt)) {
                requireNonNull(dest).add(stmt);
            }
        }
        replace(body, newBody);
    }

    private boolean isRedundantBreak(PsiStatement statement) {
        if (statement instanceof PsiBreakStatement) {
            var nextStmt = TranspileUtil.getNextStatement(statement);
            return nextStmt == null || nextStmt instanceof PsiSwitchLabelStatement;
        } else {
            return false;
        }
    }


}

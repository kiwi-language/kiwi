package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.createStatementFromText;

public class SwitchLabelStatementTransformer extends VisitorBase {

    @Override
    public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
        super.visitSwitchLabelStatement(statement);
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        if (isColonSwitch(statement)) {
            var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
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
            replace(statement.getBody(), newBody);
        } else {
            super.visitSwitchStatement(statement);
        }
    }

    private boolean isRedundantBreak(PsiStatement statement) {
        if (statement instanceof PsiBreakStatement) {
            var nextStmt = TranspileUtil.getNextStatement(statement);
            return nextStmt == null || nextStmt instanceof PsiSwitchLabelStatement;
        } else {
            return false;
        }
    }

    private boolean isColonSwitch(PsiSwitchStatement statement) {
        var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
        return stmts.length > 0 && stmts[0] instanceof PsiSwitchLabelStatement;
    }

}

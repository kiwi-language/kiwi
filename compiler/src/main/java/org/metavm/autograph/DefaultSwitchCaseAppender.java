package org.metavm.autograph;

import com.intellij.psi.*;

import java.util.HashSet;

import static org.metavm.util.NncUtils.requireNonNull;

public class DefaultSwitchCaseAppender extends VisitorBase {

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

    private void processSwitch(PsiSwitchBlock statement) {
        if (!TranspileUtils.isColonSwitch(statement)) {
            var statements = requireNonNull(statement.getBody()).getStatements();
            boolean hasDefault = false;
            for (var stmt : statements) {
                var labelStmt = (PsiSwitchLabeledRuleStatement) stmt;
                if (labelStmt.isDefaultCase()) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault) {
                if (isAllCasesCovered(statement)) {
                    statement.getBody().addBefore(
                            TranspileUtils.createStatementFromText("default -> throw new IllegalStateException();"),
                            null
                    );
                } else {
                    statement.getBody().addBefore(
                            TranspileUtils.createStatementFromText("default -> {}"),
                            null
                    );
                }
            }
        }
    }

    private boolean isAllCasesCovered(PsiSwitchBlock statement) {
        var expr = statement.getExpression();
        if (expr.getType() instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            if (psiClass.isEnum()) {
                return isAllEnumCasesCovered(statement, psiClass);
            }
        }
        return false;
    }

    private boolean isAllEnumCasesCovered(PsiSwitchBlock statement, PsiClass psiClass) {
        var enumConstants = new HashSet<>(TranspileUtils.getEnumConstants(psiClass));
        boolean nullCovered = false;
        var stmts = requireNonNull(statement.getBody()).getStatements();
        for (var stmt : stmts) {
            var labelStmt = (PsiSwitchLabeledRuleStatement) stmt;
            if (!labelStmt.isDefaultCase()) {
                var caseElements = requireNonNull(labelStmt.getCaseLabelElementList());
                for (var caseElement : caseElements.getElements()) {
                    if (caseElement instanceof PsiReferenceExpression refExpr) {
                        var target = refExpr.resolve();
                        if (target instanceof PsiEnumConstant ec) {
                            enumConstants.remove(ec);
                        }
                    } else if (caseElement instanceof PsiLiteralExpression literalExpression) {
                        if (literalExpression.getText().equals("null")) {
                            nullCovered = true;
                        }
                    }
                }
            }
        }
        return enumConstants.isEmpty() && nullCovered;

    }
}

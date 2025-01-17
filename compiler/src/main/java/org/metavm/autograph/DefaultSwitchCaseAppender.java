package org.metavm.autograph;

import com.intellij.psi.*;

import java.util.HashSet;
import java.util.Objects;

public class DefaultSwitchCaseAppender extends SkipDiscardedVisitor {

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
            var statements = Objects.requireNonNull(statement.getBody()).getStatements();
            boolean hasDefault = false;
            for (var stmt : statements) {
                if(stmt instanceof PsiSwitchLabelStatement labelStmt) {
                    if (labelStmt.isDefaultCase()) {
                        hasDefault = true;
                        break;
                    }
                }
            }
            if (!hasDefault) {
                statement.getBody().addBefore(
                        TranspileUtils.createStatementFromText("default:"),
                        null
                );
                if (isAllCasesCovered(statement)) {
                    statement.getBody().addBefore(
                            TranspileUtils.createStatementFromText("throw new IllegalStateException(\"Match error\");"),
                            null
                    );
                }
            }
        }
    }

    private boolean isAllCasesCovered(PsiSwitchBlock statement) {
        var expr = statement.getExpression();
        if (expr.getType() instanceof PsiClassType classType) {
            var psiClass = Objects.requireNonNull(classType.resolve());
            if (psiClass.isEnum()) {
                return isAllEnumCasesCovered(statement, psiClass);
            }
        }
        return false;
    }

    private boolean isAllEnumCasesCovered(PsiSwitchBlock statement, PsiClass psiClass) {
        var enumConstants = new HashSet<>(TranspileUtils.getEnumConstants(psiClass));
        boolean nullCovered = false;
        var stmts = Objects.requireNonNull(statement.getBody()).getStatements();
        for (var stmt : stmts) {
            var labelStmt = (PsiSwitchLabeledRuleStatement) stmt;
            if (!labelStmt.isDefaultCase()) {
                var caseElements = Objects.requireNonNull(labelStmt.getCaseLabelElementList());
                for (var caseElement : caseElements.getElements()) {
                    if (caseElement instanceof PsiReferenceExpression refExpr) {
                        var target = refExpr.resolve();
                        if (target instanceof PsiField ec && TranspileUtils.isEnumConstant(ec)) {
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

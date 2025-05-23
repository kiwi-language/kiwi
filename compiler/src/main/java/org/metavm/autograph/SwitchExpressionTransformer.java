package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.util.LinkedList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.createStatementFromText;

public class SwitchExpressionTransformer extends VisitorBase {

    private static final LinkedList<SwitchInfo> switches = new LinkedList<>();

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        enterSwitchExpr(expression);
        var enclosingStmt = TranspileUtils.getEnclosingStatement(expression);
        String varDeclTest = requireNonNull(expression.getType()).getCanonicalText() + " " + currentSwitchExpr().resultVar + ";";
        insertBefore(createStatementFromText(varDeclTest), enclosingStmt);
        String switchText = "switch (" + requireNonNull(expression.getExpression()).getText() + "){}";
        var switchStmt = (PsiSwitchStatement) TranspileUtils.createStatementFromText(switchText);
        switchStmt = (PsiSwitchStatement) insertBefore(switchStmt, enclosingStmt);
        var newBody = Objects.requireNonNull(switchStmt.getBody());
        currentSwitchExpr().newBody = newBody;
        var body = Objects.requireNonNull(expression.getBody());
        currentSwitchExpr().oldBody = body;
        if (body.getStatementCount() > 0) {
            var element = body.getFirstBodyElement();
            while (element != null) {
                element.accept(this);
                element = getReplacement(element);
                if (!(element instanceof PsiWhiteSpace)) {
                    newBody.add(element.copy());
                }
                element = element.getNextSibling();
                if (element == body.getLastBodyElement()) {
                    break;
                }
            }
        }
        var switchExprType = expression.getExpression().getType();
        if (switchExprType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            if (psiClass.isEnum()) {
                if (!currentSwitchExpr().defaultPresent
                        && !currentSwitchExpr().nullCovered
                        && currentSwitchExpr().coveredEnumConstants.size() == TranspileUtils.getEnumConstants(psiClass).size()) {
                    if (TranspileUtils.isColonSwitch(switchStmt)) {
                        newBody.addBefore(
                                createStatementFromText("case null: "),
                                null
                        );
                        newBody.addBefore(
                                createStatementFromText("throw new IllegalStateException();"),
                                null
                        );
                    } else
                        newBody.addBefore(
                                createStatementFromText("case null -> throw new IllegalStateException();"),
                                null
                        );
                }
            }
        }
        replace(expression, TranspileUtils.createExpressionFromText(currentSwitchExpr().resultVar));
        exitSwitchExpr();
    }

    private void enterSwitchExpr(PsiSwitchExpression expression) {
        var scope = requireNonNull(expression.getUserData(Keys.BODY_SCOPE));
        switches.push(new SwitchInfo(namer.newName("switchResult", scope.getAllDefined())));
    }

    private void exitSwitchExpr() {
        switches.pop();
    }

    private SwitchInfo currentSwitchExpr() {
        return Objects.requireNonNull(switches.peek());
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        super.visitYieldStatement(statement);
        var switchElement = TranspileUtils.findParent(statement, Set.of(PsiSwitchStatement.class, PsiSwitchExpression.class));
        if (switchElement instanceof PsiSwitchExpression switchExpr) {
            var expr = Objects.requireNonNull(statement.getExpression());
            var replacement = (PsiStatement) replace(statement,
                    createStatementFromText(currentSwitchExpr().resultVar + " = " + expr.getText() + ";"));
            if (TranspileUtils.isColonSwitch(switchExpr)) {
                insertAfter(createStatementFromText("break;"), replacement);
            }
        }
    }

    @Override
    public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
        if (statement.getParent().getParent() instanceof PsiSwitchExpression) {
            if (statement.isDefaultCase())
                currentSwitchExpr().defaultPresent = true;
            else
                processCaseLabelElementList(requireNonNull(statement.getCaseLabelElementList()));
        }
        super.visitSwitchLabelStatement(statement);
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        if (statement.getParent().getParent() instanceof PsiSwitchExpression) {
            if (statement.isDefaultCase())
                currentSwitchExpr().defaultPresent = true;
            else
                processCaseLabelElementList(requireNonNull(statement.getCaseLabelElementList()));
            if (statement.getBody() instanceof PsiExpressionStatement exprStmt) {
                var expr = exprStmt.getExpression();
                var assignment = createStatementFromText(currentSwitchExpr().resultVar + " = " + expr.getText() + ";");
                replace(exprStmt, assignment);
            }
            else
                super.visitSwitchLabeledRuleStatement(statement);
        }
    }

    private void processCaseLabelElementList(PsiCaseLabelElementList caseLabelElementList) {
        for (PsiCaseLabelElement element : caseLabelElementList.getElements()) {
            if (element instanceof PsiReferenceExpression refExpr) {
                var refTarget = refExpr.resolve();
                if (refTarget instanceof PsiEnumConstant enumConstant)
                    currentSwitchExpr().coveredEnumConstants.add(enumConstant);
            } else if (element instanceof PsiLiteralExpression literalExpression) {
                if (literalExpression.getText().equals("null")) {
                    currentSwitchExpr().nullCovered = true;
                }
            }
        }
    }

    private static class SwitchInfo {
        private final String resultVar;
        private PsiCodeBlock oldBody;
        private PsiCodeBlock newBody;
        private final Set<PsiEnumConstant> coveredEnumConstants = new HashSet<>();
        private boolean nullCovered;
        private boolean defaultPresent;

        private SwitchInfo(String resultVar) {
            this.resultVar = resultVar;
        }
    }

}

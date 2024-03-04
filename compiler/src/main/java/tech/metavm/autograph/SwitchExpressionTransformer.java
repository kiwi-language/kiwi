package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.createStatementFromText;

public class SwitchExpressionTransformer extends VisitorBase {

    private static final LinkedList<SwitchInfo> switches = new LinkedList<>();

    private final NameTracker nameTracker = new NameTracker();

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        enterSwitchExpr(expression);
        var enclosingStmt = TranspileUtil.getEnclosingStatement(expression);
        String varDeclTest = requireNonNull(expression.getType()).getCanonicalText() + " " + currentSwitchExpr().resultVar + ";";
        insertBefore(createStatementFromText(varDeclTest), enclosingStmt);
        String switchText = "switch (" + requireNonNull(expression.getExpression()).getText() + "){}";
        var switchStmt = (PsiSwitchStatement) TranspileUtil.createStatementFromText(switchText);
        switchStmt = (PsiSwitchStatement) insertBefore(switchStmt, enclosingStmt);
        var newBody = NncUtils.requireNonNull(switchStmt.getBody());
        currentSwitchExpr().newBody = newBody;
        var body = NncUtils.requireNonNull(expression.getBody());
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
                        && currentSwitchExpr().coveredEnumConstants.size() == TranspileUtil.getEnumConstants(psiClass).size()) {
                    if (TranspileUtil.isColonSwitch(switchStmt)) {
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
        replace(expression, TranspileUtil.createExpressionFromText(currentSwitchExpr().resultVar));
        exitSwitchExpr();
    }

    private void enterSwitchExpr(PsiSwitchExpression expression) {
        switches.push(new SwitchInfo(nameTracker.nextName("switchResult")));
    }

    private void exitSwitchExpr() {
        switches.pop();
    }

    private SwitchInfo currentSwitchExpr() {
        return NncUtils.requireNonNull(switches.peek());
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        super.visitYieldStatement(statement);
        var switchElement = TranspileUtil.getParent(statement, Set.of(PsiSwitchStatement.class, PsiSwitchExpression.class));
        if (switchElement instanceof PsiSwitchExpression switchExpr) {
            var expr = NncUtils.requireNonNull(statement.getExpression());
            var replacement = (PsiStatement) replace(statement,
                    createStatementFromText(currentSwitchExpr().resultVar + " = " + expr.getText() + ";"));
            if (TranspileUtil.isColonSwitch(switchExpr)) {
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
        } else {
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

    @Override
    public void visitMethod(PsiMethod method) {
        nameTracker.enterMethod();
        super.visitMethod(method);
        nameTracker.exitMethod();
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        nameTracker.enterBlock();
        super.visitCodeBlock(block);
        nameTracker.exitBlock();
    }

    @Override
    public void visitParameter(PsiParameter parameter) {
        nameTracker.addName(parameter.getName());
        super.visitParameter(parameter);
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        nameTracker.addName(variable.getName());
        super.visitLocalVariable(variable);
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

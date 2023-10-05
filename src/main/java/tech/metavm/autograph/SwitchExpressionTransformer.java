package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.expression.CodeBlock;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import java.util.Objects;

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
        replace(expression, TranspileUtil.createExpressionFromText(currentSwitchExpr().resultVar));
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
        var expr = NncUtils.requireNonNull(statement.getExpression());
        replace(statement,
                createStatementFromText(currentSwitchExpr().resultVar + " = " + expr.getText() + ";"));
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        if (statement.getParent().getParent() instanceof PsiSwitchExpression
                && statement.getBody() instanceof PsiExpressionStatement exprStmt) {
            var expr = exprStmt.getExpression();
            var assignment = createStatementFromText(currentSwitchExpr().resultVar + " = " + expr.getText() + ";");
            replace(exprStmt, assignment);
        } else {
            super.visitSwitchLabeledRuleStatement(statement);
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

        private SwitchInfo(String resultVar) {
            this.resultVar = resultVar;
        }
    }

}

package org.metavm.autograph;

import com.intellij.psi.PsiConditionalExpression;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiStatement;

import static org.metavm.util.NncUtils.requireNonNull;

public class ConditionalExpressionTransformer extends VisitorBase {

    @Override
    public void visitConditionalExpression(PsiConditionalExpression expression) {
        var statement = requireNonNull(TranspileUtils.findParent(expression, PsiStatement.class));
        var scope = requireNonNull(statement.getUserData(Keys.SCOPE));
        var varName = namer.newName("conditional", scope.getAllDefined());
        var varDecl = (PsiDeclarationStatement) TranspileUtils.createStatementFromText(String.format("%s %s;",
                requireNonNull(expression.getType()).getCanonicalText(), varName));
        insertBefore(varDecl, statement);
        String text = """
                if(%s) {
                    %s = %s;
                } else {
                    %s = %s;
                }
                """.formatted(expression.getCondition().getText(),
                varName, requireNonNull(expression.getThenExpression()).getText(),
                varName, requireNonNull(expression.getElseExpression()).getText());
        var branch = TranspileUtils.createStatementFromText(text);
        insertBefore(branch, statement);
        replace(expression, TranspileUtils.createExpressionFromText(varName));
    }

}

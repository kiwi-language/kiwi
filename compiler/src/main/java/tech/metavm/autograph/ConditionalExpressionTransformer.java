package tech.metavm.autograph;

import com.intellij.psi.PsiConditionalExpression;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiStatement;

import static tech.metavm.util.NncUtils.requireNonNull;

public class ConditionalExpressionTransformer extends VisitorBase {

    @Override
    public void visitConditionalExpression(PsiConditionalExpression expression) {
        var statement = requireNonNull(TranspileUtil.getParent(expression, PsiStatement.class));
        var scope = requireNonNull(statement.getUserData(Keys.SCOPE));
        var varName = namer.newName("conditional", scope.getAllDefined());
        var varDecl = (PsiDeclarationStatement) TranspileUtil.createStatementFromText(String.format("%s %s;",
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
        var branch = TranspileUtil.createStatementFromText(text);
        insertBefore(branch, statement);
        replace(expression, TranspileUtil.createExpressionFromText(varName));
    }

}

package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ForTransformer extends VisitorBase {

    private final Map<String, String> variableMap = new HashMap<>();

    @Override
    public void visitForStatement(PsiForStatement statement) {
        var parent = TranspileUtils.getParentRequired(statement, Set.of(PsiMethod.class, PsiStatement.class));
        var scope = TranspileUtils.getBodyScope(parent);
        var init = statement.getInitialization();
        var curVariableMap = new HashMap<String, String>();
        if (init != null) {
            if (init instanceof PsiDeclarationStatement declStmt) {
                for (PsiElement element : declStmt.getDeclaredElements()) {
                    PsiVariable variable = (PsiVariable) element;
                    var varName = requireNonNull(variable.getName());
                    var newName = namer.newName(varName, scope.getAllDefined());
                    curVariableMap.put(varName, newName);
                    String declText =
                            variable.getInitializer() == null ?
                                    String.format("%s %s;", variable.getType().getPresentableText(), newName)
                                    : String.format("%s %s = %s;", variable.getType().getPresentableText(), newName, variable.getInitializer().getText());
                    insertBefore(TranspileUtils.createStatementFromText(declText), statement);
                }
            } else {
                List<PsiStatement> initStmts = tryBreakupStatements(init);
                for (PsiStatement initStmt : initStmts) {
                    insertBefore(initStmt, statement);
                }
            }
        }
        variableMap.putAll(curVariableMap);
        super.visitForStatement(statement);
        variableMap.keySet().removeAll(curVariableMap.keySet());
        PsiBlockStatement body = statement.getBody() != null ?
                convertToBlockStatement((PsiStatement) statement.getBody().copy())
                : (PsiBlockStatement) TranspileUtils.createStatementFromText("{}");
        if (statement.getUpdate() != null) {
            List<PsiStatement> updateStmts = tryBreakupStatements(statement.getUpdate());
            for (PsiStatement updateStmt : updateStmts) {
                body.getCodeBlock().add(updateStmt);
            }
        }
        String condText = statement.getCondition() != null ? statement.getCondition().getText() : "true";
        PsiWhileStatement whileStmt =
                (PsiWhileStatement) TranspileUtils.createStatementFromText("while (" + condText + ") {}");
        if (statement.getBody() != null) {
            NncUtils.requireNonNull(whileStmt.getBody()).replace(body);
        }
        replace(statement, whileStmt);
    }

    private List<PsiStatement> tryBreakupStatements(PsiStatement statement) {
        if (statement instanceof PsiExpressionListStatement exprListStmt) {
            return NncUtils.map(
                    exprListStmt.getExpressionList().getExpressions(),
                    expr -> TranspileUtils.createStatementFromText(expr.getText() + ";")
            );
        } else {
            if (!statement.getText().trim().endsWith(";")) {
                return List.of(TranspileUtils.createStatementFromText(statement.getText() + ";"));
            } else {
                return List.of(statement);
            }
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);
        var target = expression.resolve();
        if (target instanceof PsiLocalVariable variable) {
            var newName = variableMap.get(variable.getName());
            if (newName != null) {
                expression.handleElementRename(newName);
            }
        }
    }
}

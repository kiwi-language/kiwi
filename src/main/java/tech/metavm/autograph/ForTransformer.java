package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ForTransformer extends VisitorBase {

    private final NameTracker nameTracker = new NameTracker();

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitForStatement(statement);

        PsiBlockStatement body = statement.getBody() != null ?
                convertToBlockStatement((PsiStatement) statement.getBody().copy())
                : (PsiBlockStatement) TranspileUtil.createStatementFromText("{}");
        var init = statement.getInitialization();
        if (init != null) {
            if (init instanceof PsiDeclarationStatement declStmt) {
                for (PsiElement element : declStmt.getDeclaredElements()) {
                    PsiVariable variable = (PsiVariable) element;
                    nameTracker.addName(variable.getName());
                }
                insertBefore((PsiStatement) declStmt.copy(), statement);
            } else {
                List<PsiStatement> initStmts = tryBreakupStatements(init);
                for (PsiStatement initStmt : initStmts) {
                    insertBefore(initStmt, statement);
                }
            }
        }
        if (statement.getUpdate() != null) {
            List<PsiStatement> updateStmts = tryBreakupStatements(statement.getUpdate());
            for (PsiStatement updateStmt : updateStmts) {
                body.getCodeBlock().add(updateStmt);
            }
        }
        String condText = statement.getCondition() != null ? statement.getCondition().getText() : "true";
        PsiWhileStatement whileStmt =
                (PsiWhileStatement) TranspileUtil.createStatementFromText("while (" + condText + ") {}");
        if (statement.getBody() != null) {
            NncUtils.requireNonNull(whileStmt.getBody()).replace(body);
        }
        replace(statement, whileStmt);
    }

    private List<PsiStatement> tryBreakupStatements(PsiStatement statement) {
        if (statement instanceof PsiExpressionListStatement exprListStmt) {
            return NncUtils.map(
                    exprListStmt.getExpressionList().getExpressions(),
                    expr -> TranspileUtil.createStatementFromText(expr.getText() + ";")
            );
        } else {
            if (!statement.getText().trim().endsWith(";")) {
                return List.of(TranspileUtil.createStatementFromText(statement.getText() + ";"));
            } else {
                return List.of(statement);
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
    public void visitLocalVariable(PsiLocalVariable variable) {
        if (nameTracker.isVisible(variable.getName())) {
            var newName = nameTracker.nextName(variable.getName());
            nameTracker.rename(variable.getName(), newName);
            variable.setName(newName);
        } else {
            nameTracker.addName(variable.getName());
        }
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        nameTracker.enterBlock();
        super.visitCodeBlock(block);
        nameTracker.exitBlock();
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        var target = expression.resolve();
        if (target instanceof PsiVariable variable) {
            String mappedName;
            if ((mappedName = nameTracker.getMappedName(variable.getName())) != null) {
                replace(expression, TranspileUtil.createExpressionFromText(mappedName));
            }
        }
    }


}

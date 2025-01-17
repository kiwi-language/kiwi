package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ForTransformer extends SkipDiscardedVisitor {

    private final Map<String, String> variableMap = new HashMap<>();
    private LoopInfo currentLoop;

    @Override
    public void visitForStatement(PsiForStatement statement) {
        var parent = TranspileUtils.getParentNotNull(statement, Set.of(PsiMethod.class, PsiStatement.class));
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
                    if(!(initStmt instanceof PsiEmptyStatement))
                        insertBefore(initStmt, statement);
                }
            }
        }
        var loopInfo = enterLoop(statement);
        variableMap.putAll(curVariableMap);
        statement.accept(new RenameVisitor());
        variableMap.keySet().removeAll(curVariableMap.keySet());
        List<PsiStatement> updateStmts = statement.getUpdate() != null ?
                tryBreakupStatements(statement.getUpdate()) : List.of();
        loopInfo.updateStatements.addAll(updateStmts);
        variableMap.putAll(curVariableMap);
        super.visitForStatement(statement);
        variableMap.keySet().removeAll(curVariableMap.keySet());
        exitLoop();
        PsiBlockStatement body = statement.getBody() != null ?
                convertToBlockStatement((PsiStatement) statement.getBody().copy())
                : (PsiBlockStatement) TranspileUtils.createStatementFromText("{}");
        for (PsiStatement updateStmt : updateStmts) {
            body.getCodeBlock().add(updateStmt);
        }
        String condText = statement.getCondition() != null ? statement.getCondition().getText() : "true";
        PsiWhileStatement whileStmt =
                (PsiWhileStatement) TranspileUtils.createStatementFromText("while (" + condText + ") {}");
        if (statement.getBody() != null) {
            Objects.requireNonNull(whileStmt.getBody()).replace(body);
        }
        replace(statement, whileStmt);
    }

    private List<PsiStatement> tryBreakupStatements(PsiStatement statement) {
        if (statement instanceof PsiExpressionListStatement exprListStmt) {
            return Utils.map(
                    exprListStmt.getExpressionList().getExpressions(),
                    expr -> TranspileUtils.createStatementFromText(expr.getText() + ";")
            );
        } else {
            if (!statement.getText().trim().endsWith(";")) {
                return List.of(TranspileUtils.createStatementFromText(statement.getText() + ";"));
            } else {
                return List.of((PsiStatement) statement.copy());
            }
        }
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        enterLoop(statement);
        super.visitDoWhileStatement(statement);
        exitLoop();
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterLoop(statement);
        super.visitForeachStatement(statement);
        exitLoop();
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterLoop(statement);
        super.visitWhileStatement(statement);
        exitLoop();
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        super.visitContinueStatement(statement);
        var label = Utils.safeCall(statement.getLabelIdentifier(), PsiElement::getText);
        var loopInfo = currentLoop();
        while (loopInfo != null && !loopInfo.matchLabel(label)) {
            loopInfo = loopInfo.parent;
        }
        Objects.requireNonNull(loopInfo, "Cannot find continued loop");
        for (PsiStatement updateStatement : loopInfo.updateStatements) {
            insertBefore(updateStatement, statement);
        }
    }

    private LoopInfo enterLoop(PsiStatement statement) {
        return currentLoop = new LoopInfo(statement, currentLoop);
    }

    private void exitLoop() {
        currentLoop = requireNonNull(currentLoop).parent;
    }

    private LoopInfo currentLoop() {
        return Objects.requireNonNull(currentLoop);
    }

    private static class LoopInfo {
        final @Nullable LoopInfo parent;
        final PsiStatement statement;
        final @Nullable String label;
        private final List<PsiStatement> updateStatements = new ArrayList<>();

        private LoopInfo(PsiStatement statement, @Nullable LoopInfo parent) {
            this.statement = statement;
            this.parent = parent;
            this.label = TranspileUtils.getLabel(statement);
        }

        boolean matchLabel(@Nullable String label) {
            return label == null || Objects.equals(this.label, label);
        }

        boolean hasEnclosingForLoop() {
            var p = parent;
            while (p != null) {
                if(p.statement instanceof PsiForStatement)
                    return true;
                p = p.parent;
            }
            return false;
        }

    }

    private class RenameVisitor extends VisitorBase {

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

}

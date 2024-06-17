package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.metavm.util.NncUtils.requireNonNull;

public class BreakTransformer extends VisitorBase {

    private LoopInfo loopInfo;

//    private final NameTracker nameTracker = new NameTracker();

    private void defineBreakVar(PsiStatement statement) {
        var loop = currentLoopInfo();
        var breakVarDecl = TranspileUtils.createStatementFromText("boolean " + loop.breakVar + " = false;");
        insertBefore(breakVarDecl, statement);
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var loop = enterLoop(statement);
        var body = statement.getBody();
        if (body != null) {
            body.accept(this);
            if (loop.breakUsed) {
                defineBreakVar(statement);
                String text = EXTRA_LOOP_TEST + "(" + loop.getConditionText() + ");";
                var noop = TranspileUtils.createStatementFromText(text);
                prependBody(body, noop);
            }
        }
        exitLoop();
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        var loop = enterLoop(statement);
        if(statement.getBody() != null) {
            statement.getBody().accept(this);
            if(loop.breakUsed) {
                defineBreakVar(statement);
                var cond = TranspileUtils.createExpressionFromText(loop.getConditionText());
                var currentCond = requireNonNull(statement.getCondition());
                currentCond.replace(TranspileUtils.and(currentCond, cond));
            }
        }
        exitLoop();
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        var loop = enterLoop(statement);
        if(statement.getBody() != null) {
            statement.getBody().accept(this);
            if(loop.breakUsed) {
                defineBreakVar(statement);
                var cond = TranspileUtils.createExpressionFromText(loop.getConditionText());
                TranspileUtils.replaceForCondition(statement, cond);
            }
        }
        exitLoop();
    }

    private LoopInfo currentLoopInfo() {
        return requireNonNull(loopInfo);
    }

    private LoopInfo enterLoop(PsiStatement element) {
        var scope = requireNonNull(element.getUserData(Keys.BODY_SCOPE));
        return loopInfo = new LoopInfo(element, loopInfo, namer.newName("break_", scope.getAllDefined()));
    }

    private void exitLoop() {
        loopInfo = currentLoopInfo().parent;
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        String label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var loop = currentLoopInfo();
        loop.breakUsed = true;
        if (label != null) {
            while (loop != null && !Objects.equals(loop.label, label)) {
                loop = loop.parent;
                if (loop != null) {
                    loop.breakUsed = true;
                }
            }
            requireNonNull(loop, "Can not find an enclosing loop with label '" + label + "'");
        }
        String text = loop.breakVar + " = true;";
        var replacement = replace(statement, TranspileUtils.createStatementFromText(text));
        String continueText = "continue" + (label != null ? " " + label : "") + ";";
        var continueStmt = TranspileUtils.createStatementFromText(continueText);
        insertAfter(continueStmt, (PsiStatement) replacement);
    }

    @SuppressWarnings("unused")
    private int getChildIndex(PsiElement child) {
        var parent = child.getParent();
        for (int i = 0; i < parent.getChildren().length; i++) {
            if (parent.getChildren()[i] == child) {
                return i;
            }
        }
        return -1;
    }

    private static class LoopInfo {

        @Nullable
        final LoopInfo parent;
        final String label;
        boolean breakUsed;
        final String breakVar;

        private LoopInfo(PsiElement element, @Nullable LoopInfo parent, String breakVar) {
            this.parent = parent;
            if (element.getParent() instanceof PsiLabeledStatement labeledStatement) {
                label = labeledStatement.getLabelIdentifier().getText();
            } else {
                label = null;
            }
            this.breakVar = breakVar;
        }

        List<LoopInfo> allEnclosingLoops() {
            List<LoopInfo> loops = new ArrayList<>();
            LoopInfo loop = this;
            while (loop != null) {
                loops.add(loop);
                loop = loop.parent;
            }
            return loops;
        }

        String getConditionText() {
            var loops = allEnclosingLoops();
            return NncUtils.join(loops, l -> "!" + l.breakVar, " && ");
        }

    }

}



package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.util.NncUtils.requireNonNull;

@Slf4j
public class BreakTransformer extends SkipDiscardedVisitor {

    private BlockInfo blockInfo;

    private PsiStatement defineBreakVar() {
        return TranspileUtils.createStatementFromText("boolean " + currentLoopInfo().getBreakVar() + " = false;");
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var loop = enterLoop(statement);
        var body = statement.getBody();
        if (body != null) {
            body.accept(this);
            if (loop.breakUsed) {
                var breakVarDecl = defineBreakVar();
                String text = EXTRA_LOOP_TEST + "(" + loop.getConditionText() + ");";
                var noop = TranspileUtils.createStatementFromText(text);
                prependBody(body, noop);
                replace(statement, TranspileUtils.createBlockStatement(breakVarDecl, statement));
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
                var breakVarDecl = defineBreakVar();
                var cond = TranspileUtils.createExpressionFromText(loop.getConditionText());
                var currentCond = requireNonNull(statement.getCondition());
                currentCond.replace(TranspileUtils.and(currentCond, cond));
                replace(statement, TranspileUtils.createBlockStatement(breakVarDecl, statement));
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
                var breakVarDecl = defineBreakVar();
                var cond = TranspileUtils.createExpressionFromText(loop.getConditionText());
                TranspileUtils.replaceForCondition(statement, cond);
                replace(statement, TranspileUtils.createBlockStatement(breakVarDecl, statement));
            }
        }
        exitLoop();
    }

    private BlockInfo currentLoopInfo() {
        return requireNonNull(blockInfo);
    }

    private BlockInfo enterLoop(PsiStatement element) {
        var scope = requireNonNull(element.getUserData(Keys.BODY_SCOPE));
        var breakVar = TranspileUtils.isBreakable(element) ?
                namer.newName("break_", scope.getAllDefined()) : null;
        return blockInfo = new BlockInfo(element, blockInfo, breakVar);
    }

    private void exitLoop() {
        blockInfo = currentLoopInfo().parent;
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        String label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var loop = currentLoopInfo();
        do {
            loop.breakUsed = true;
            loop = loop.parent;
        } while (loop != null && !loop.matchLabel(label));
        requireNonNull(loop, "Can not find an enclosing loop with label '" + label + "'");
        loop.breakUsed = true;
        currentLoopInfo().useBreak(loop.getBreakVar(), label);
        String text = loop.getBreakVar() + " = true;";
        replace(statement, TranspileUtils.createStatementFromText(text));
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

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        var loop = enterLoop(statement);
        statement = (PsiBlockStatement) replace(statement, visitBlock(statement));
        if (loop.isBreakable() && loop.breakUsed) {
            statement.getCodeBlock().addAfter(defineBreakVar(), null);
        }
        exitLoop();
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        var blockInfo = enterLoop(statement);
        super.visitIfStatement(statement);
        if(blockInfo.isBreakable() && blockInfo.breakUsed) {
            var breakVarDecl = defineBreakVar();
            replace(statement, TranspileUtils.createBlockStatement(breakVarDecl, statement));
        }
        exitLoop();
    }

    private PsiElement visitBlock(PsiStatement body) {
        List<PsiStatement> statements = TranspileUtils.extractBody(body);
        PsiBlockStatement result = (PsiBlockStatement) TranspileUtils.createStatementFromText("{}");
        PsiCodeBlock dest = result.getCodeBlock();
        for (PsiStatement stmt : statements) {
            var block = currentLoopInfo();
            boolean breakUsed = block.isBreakUsed();
            String cond = block.getConditionTextForUsedBreaks();
            block.clearUsedBreaks();
            stmt.accept(this);
            stmt = (PsiStatement) getReplacement(stmt);
            if (breakUsed) {
                String text = "if (" + cond + ") {}";
                PsiIfStatement ifStmt = (PsiIfStatement) TranspileUtils.createStatementFromText(text);
                ifStmt = (PsiIfStatement) replace(stmt, ifStmt);
                ifStmt = (PsiIfStatement) dest.add(ifStmt);
                dest = ((PsiBlockStatement) requireNonNull(ifStmt.getThenBranch())).getCodeBlock();
                dest.add(stmt.copy());
            } else {
                try {
                    dest.add(stmt);
                }
                catch (Exception e) {
                    log.debug("Target element: {}, parent: {}", stmt.getText(), stmt.getParent());
                    throw e;
                }
            }
        }
        return result;
    }

    private static class BlockInfo {

        @Nullable
        final BlockInfo parent;
        final String label;
        boolean breakUsed;
        final @Nullable String breakVar;
        final PsiElement element;
        final Set<String> usedBreaks = new HashSet<>();
        final boolean breakable;

        private BlockInfo(PsiElement element, @Nullable BlockInfo parent, @Nullable String breakVar) {
            this.element = element;
            this.parent = parent;
            if (element.getParent() instanceof PsiLabeledStatement labeledStatement) {
                label = labeledStatement.getLabelIdentifier().getText();
            } else {
                label = null;
            }
            this.breakVar = breakVar;
            breakable = TranspileUtils.isBreakable(element);
        }

        List<BlockInfo> allEnclosingLoops() {
            List<BlockInfo> loops = new ArrayList<>();
            BlockInfo loop = this;
            while (loop != null) {
                if(loop.isBreakable())
                    loops.add(loop);
                loop = loop.parent;
            }
            return loops;
        }

        void useBreak(String variable, @Nullable String label) {
            var block = this;
            while (block != null) {
                block.usedBreaks.add(variable);
                if (block.isBreakable() && block.matchLabel(label)) {
                    break;
                }
                block = block.parent;
            }
        }

        private boolean matchLabel(String label) {
            return label == null ? isLoop() : Objects.equals(this.label, label);
        }

        String getBreakVar() {
            return Objects.requireNonNull(breakVar, () -> "Element " + element.getText() + " is not breakable");
        }

        boolean isBreakUsed() {
            return !usedBreaks.isEmpty();
        }

        void clearUsedBreaks() {
            usedBreaks.clear();
        }

        String getConditionTextForUsedBreaks() {
            return NncUtils.join(usedBreaks, v -> "!" + v, " && ");
        }

        String getConditionText() {
            var loops = allEnclosingLoops();
            return NncUtils.join(loops, l -> "!" + l.getBreakVar(), " && ");
        }

        boolean isBreakable() {
            return breakable;
        }

        boolean isLoop() {
            return TranspileUtils.isLoop(element);
        }

    }

}



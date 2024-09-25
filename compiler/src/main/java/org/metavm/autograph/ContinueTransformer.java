package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.metavm.util.NncUtils.requireNonNull;

@Slf4j
public class ContinueTransformer extends SkipDiscardedVisitor {

//    private final NameTracker nameTracker = new NameTracker();
    private Loop loopInfo;
    @Nullable
    private BlockInfo blockInfo;

    @Override
    public void visitMethod(PsiMethod method) {
//        nameTracker.enterMethod();
        enterBlock(false, null);
        super.visitMethod(method);
        exitBlock();
//        nameTracker.exitMethod();
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        String label = NncUtils.get(statement.getLabelIdentifier(), PsiIdentifier::getText);
        var loop = currentLoop();
        if (label != null) {
            while (loop != null && !Objects.equals(loop.label, label)) {
                loop = loop.parent;
            }
            requireNonNull(loop, "Can not find an enclosing loop with label '" + label + "'");
        }
        loop.continueUsed = true;
        var block = currentBlockInfo();
        block.useContinue(loop.continueVar, label);
        String text = loop.continueVar + " = true;";
        var assignment = TranspileUtils.getElementFactory().createStatementFromText(text, null);
        replace(statement, assignment);
    }

    private void enterBlock(boolean isLoop, @Nullable String label) {
        blockInfo = new BlockInfo(isLoop, label, blockInfo);
    }

    private BlockInfo currentBlockInfo() {
        return requireNonNull(blockInfo);
    }

    private void exitBlock() {
        blockInfo = currentBlockInfo().parent;
    }

    private Loop currentLoop() {
        return requireNonNull(loopInfo);
    }

    private void enterLoop(PsiStatement statement) {
        var scope = requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
        loopInfo = new Loop(statement, loopInfo, namer.newName("continue_", scope.getAllDefined()));
    }

    private void exitLoop() {
        loopInfo = loopInfo.parent;
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterLoop(statement);
        if (statement.getBody() != null) {
            visitLoopBody(statement.getBody(), getLabel(statement));
            var block = currentBlockInfo();
            if (block.continueUsed()) {
                var firstStmt = getFirstStatement(statement.getBody());
                if (isExtraLoopTest(firstStmt)) {
                    PsiMethodCallExpression methodCallExpr =
                            (PsiMethodCallExpression) ((PsiExpressionStatement) firstStmt).getExpression();
                    String cond = "(" + methodCallExpr.getArgumentList().getExpressions()[0].getText()
                            + ") && (" + block.getConditionText() + ")";
                    String text = EXTRA_LOOP_TEST + "(" + cond + ");";
                    var extraLoopTest = TranspileUtils.createStatementFromText(text);
                    firstStmt.replace(extraLoopTest);
                } else {
                    String text = EXTRA_LOOP_TEST + "(" + block.getConditionText() + ");";
                    var extraLoopTest = TranspileUtils.createStatementFromText(text);
                    prependBody(statement.getBody(), extraLoopTest);
                }
            }
        }
        exitLoop();
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterLoop(statement);
        if (statement.getBody() != null) {
            visitLoopBody(statement.getBody(), getLabel(statement));
            var block = currentBlockInfo();
            if (block.continueUsed()) {
                var cond = TranspileUtils.createExpressionFromText(block.getConditionText());
                var currentCond = Objects.requireNonNull(statement.getCondition());
                currentCond.replace(TranspileUtils.and(currentCond, cond));
            }
        }
        exitLoop();
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        if (statement.getThenBranch() != null) {
            visitNonLoopBody(statement.getThenBranch());
        }
        if (statement.getElseBranch() != null) {
            visitNonLoopBody(statement.getElseBranch());
        }
    }

    private void visitLoopBody(PsiStatement body, @Nullable String label) {
//        if(body instanceof PsiCodeBlock) {
//            nameTracker.enterBlock();
//        }
        enterBlock(true, label);
        var replacement = replace(body, visitBlock(body));
        if (loopInfo.continueUsed) {
            String code = "boolean " + loopInfo.continueVar + " = false;";
            var toInsert = TranspileUtils.createStatementFromText(code);
            var block = ((PsiBlockStatement) replacement).getCodeBlock();
            var firstStmt = block.getStatementCount() > 0 ? block.getStatements()[0] : null;
            if (firstStmt != null && isExtraLoopTest(firstStmt)) {
                block.addAfter(toInsert, firstStmt);
            } else {
                block.addBefore(toInsert, firstStmt);
            }
        }
        exitBlock();
    }

    private void visitNonLoopBody(PsiStatement body) {
        enterBlock(false, null);
        replace(body, visitBlock(body));
        exitBlock();
    }

    private PsiElement visitBlock(PsiStatement body) {
        List<PsiStatement> statements = TranspileUtils.extractBody(body);
        final PsiBlockStatement result = (PsiBlockStatement) TranspileUtils.createStatementFromText("{}");
        PsiCodeBlock dest = result.getCodeBlock();
        for (PsiStatement stmt : statements) {
            var block = currentBlockInfo();
            boolean continueUsed = block.continueUsed();
            String cond = block.getConditionText();
            block.clearUsedContinues();
            stmt.accept(this);
            stmt = (PsiStatement) getReplacement(stmt);
            if (continueUsed) {
                String text = "if (" + cond + ") {}";
                PsiIfStatement ifStmt = (PsiIfStatement) TranspileUtils.createStatementFromText(text);
                ifStmt = (PsiIfStatement) replace(stmt, ifStmt);
                ifStmt = (PsiIfStatement) dest.add(ifStmt);
                dest = ((PsiBlockStatement) requireNonNull(ifStmt.getThenBranch())).getCodeBlock();
                dest.add(stmt.copy());
            } else {
                dest.add(stmt);
            }
        }
        return result;
    }

    private static class BlockInfo {
        private final boolean isLoop;
        @Nullable
        private final String label;
        @Nullable
        private final BlockInfo parent;
        private final Set<String> usedContinues = new HashSet<>();

        private BlockInfo(boolean isLoop, @Nullable String label, @Nullable BlockInfo parent) {
            this.isLoop = isLoop;
            this.label = label;
            this.parent = parent;
        }

        void useContinue(String variable, @Nullable String label) {
            BlockInfo block = this;
            while (block != null) {
                block.usedContinues.add(variable);
                if (block.isLoop && block.matchLabel(label)) {
                    break;
                }
                block = block.parent;
            }
        }

        private boolean matchLabel(@Nullable String label) {
            NncUtils.requireTrue(isLoop);
            return label == null || Objects.equals(label, this.label);
        }

        boolean continueUsed() {
            return !usedContinues.isEmpty();
        }

        void clearUsedContinues() {
            usedContinues.clear();
        }

        String getConditionText() {
            return NncUtils.join(usedContinues, var -> "!" + var, " && ");
        }

    }

    private static class Loop {
        private final Loop parent;
        @Nullable
        private final String label;
        private final String continueVar;
        private boolean continueUsed;

        public Loop(PsiElement loop, Loop parent, String continueVar) {
            if (loop.getParent() instanceof PsiLabeledStatement labeledStmt) {
                label = labeledStmt.getLabelIdentifier().getText();
            } else {
                label = null;
            }
            this.parent = parent;
            this.continueVar = continueVar;
        }

    }

}

package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.KeyValue;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static tech.metavm.util.NncUtils.requireNonNull;

public class ContinueTransformer extends VisitorBase {

    private final NameTracker nameTracker = new NameTracker();
    private Loop loopInfo;
    private @Nullable BlockInfo blockInfo;

    @Override
    public void visitMethod(PsiMethod method) {
        nameTracker.enterMethod();
        enterBlock(false, null);
        super.visitMethod(method);
        exitBlock();
        nameTracker.exitMethod();
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        String label = NncUtils.get(statement.getLabelIdentifier(), PsiIdentifier::getText);
        var loop = currentLoop();
        if (label != null) {
            while (loop != null && !Objects.equals(loop.label, label)) {
                loop = loop.parent;
            }
            NncUtils.requireNonNull(loop, "Can not find an enclosing loop with label '" + label + "'");
        }
        var parent = TranspileUtil.getAncestor(statement,
                PsiIfStatement.class, PsiForeachStatement.class,
                PsiWhileStatement.class, PsiSwitchStatement.class);
        loop.continueUsed = true;
        var block = currentBlockInfo();
        if(parent instanceof PsiIfStatement) {
            requireNonNull(block.parent).shouldWrapNext = true;
        }
        block.useContinue(loop.continueVar, label);
        String text = loop.continueVar + " = true;";
        var assignment = TranspileUtil.getPsiElementFactory().createStatementFromText(text, null);
        replace(statement, assignment);
    }

    private void enterBlock(boolean isLoop, @Nullable String label) {
        blockInfo = new BlockInfo(isLoop, label, blockInfo);
    }

    private BlockInfo currentBlockInfo() {
        return NncUtils.requireNonNull(blockInfo);
    }

    private void exitBlock() {
        blockInfo = currentBlockInfo().parent;
    }

    private Loop currentLoop() {
        return NncUtils.requireNonNull(loopInfo);
    }

    private void enterLoop(PsiElement element) {
        loopInfo = new Loop(element, loopInfo, nameTracker.nextName("_continue"));
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
                    var extraLoopTest = TranspileUtil.createStatementFromText(text);
                    firstStmt.replace(extraLoopTest);
                } else {
                    String text = EXTRA_LOOP_TEST + "(" + block.getConditionText() + ");";
                    var extraLoopTest = TranspileUtil.createStatementFromText(text);
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
                var cond = TranspileUtil.createExpressionFromText(block.getConditionText());
                var currentCond = Objects.requireNonNull(statement.getCondition());
                currentCond.replace(TranspileUtil.and(currentCond, cond));
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

    private List<PsiStatement> extractBody(@Nullable PsiStatement body) {
        if (body == null) {
            return List.of();
        }
        if (body instanceof PsiBlockStatement block) {
            return List.of(block.getCodeBlock().getStatements());
        } else {
            return List.of(body);
        }
    }

    private void visitLoopBody(PsiStatement body, @Nullable String label) {
        if (body instanceof PsiCodeBlock) {
            nameTracker.enterBlock();
        }
        enterBlock(true, label);
        visitBlock(body);
        var replacement = getReplacement(body);
        if (loopInfo.continueUsed) {
            String code = "boolean " + loopInfo.continueVar + " = false;";
            var toInsert = TranspileUtil.createStatementFromText(code);
            var block = ((PsiBlockStatement) replacement).getCodeBlock();
            var firstStmt = block.getStatementCount() > 0 ? block.getStatements()[0] : null;
            if (firstStmt != null && isExtraLoopTest(firstStmt)) {
                block.addAfter(toInsert, firstStmt);
            } else {
                block.addBefore(toInsert, firstStmt);
            }
        }
        exitBlock();
        if (body instanceof PsiCodeBlock) {
            nameTracker.exitBlock();
        }
    }

    private void visitNonLoopBody(PsiStatement body) {
        if (body instanceof PsiCodeBlock) {
            nameTracker.enterBlock();
        }
        enterBlock(false, null);
        visitBlock(body);
        exitBlock();
        if (body instanceof PsiCodeBlock) {
            nameTracker.exitBlock();
        }
    }

    protected void visitBlock(PsiStatement body) {
        var ref = new Object() {
            boolean continueUsed;
            String cond;
        };
        visitBlock(body,
                stmt -> {
                    var block = currentBlockInfo();
                    ref.continueUsed = block.continueUsed();
                    ref.cond = block.getConditionText();
                    block.shouldWrapCurrent = block.shouldWrapNext;
                    block.shouldWrapNext = false;
                    block.clearUsedContinues();
                },
                stmt -> {
                    var block = currentBlockInfo();
                    if (block.shouldWrapCurrent) {
                        block.shouldWrapCurrent = false;
                        var ifStmt = (PsiIfStatement) getReplacement(
                                requireNonNull(TranspileUtil.getPrevStatement(stmt))
                        );
                        PsiStatement elseBranch = ifStmt.getElseBranch();
                        PsiCodeBlock elseBlock;
                        if (!(elseBranch instanceof PsiBlockStatement)) {
                            PsiStatement oldElseBranch = elseBranch;
                            elseBranch = TranspileUtil.createStatementFromText("{}");
                            ifStmt.setElseBranch(elseBranch);
                            elseBranch = requireNonNull(ifStmt.getElseBranch());
                            elseBlock = ((PsiBlockStatement) elseBranch).getCodeBlock();
                            if (oldElseBranch != null) {
                                elseBlock.add(oldElseBranch.copy());
                            }
                        } else {
                            elseBlock = ((PsiBlockStatement) elseBranch).getCodeBlock();
                        }
                        elseBlock.add(stmt);
                        stmt.delete();
                        return new KeyValue<>(null, elseBlock);
                    } else if (ref.continueUsed) {
                        String text = "if (" + ref.cond + ") {}";
                        PsiIfStatement ifStmt = (PsiIfStatement) TranspileUtil.createStatementFromText(text);
                        ifStmt = (PsiIfStatement) replace(stmt, ifStmt);
                        var newDest = ((PsiBlockStatement) NncUtils.requireNonNull(ifStmt.getThenBranch())).getCodeBlock();
                        newDest.add(stmt.copy());
                        return new KeyValue<>(ifStmt, newDest);
                    } else {
                        return new KeyValue<>(stmt, null);
                    }
                }
        );
    }

//    protected PsiElement visitBlock(PsiStatement body) {
//        List<PsiStatement> statements = extractBody(body);
//        PsiBlockStatement result = (PsiBlockStatement) TranspileUtil.createStatementFromText("{}");
//        PsiCodeBlock dest = result.getCodeBlock();
//        for (PsiStatement stmt : statements) {
//            var block = currentBlockInfo();
//            boolean continueUsed = block.continueUsed();
//            String cond = block.getConditionText();
//            block.clearUsedContinues();
//            stmt.accept(this);
//            stmt = (PsiStatement) getReplacement(stmt);
//            if (continueUsed) {
//                String text = "if (" + cond + ") {}";
//                PsiIfStatement ifStmt = (PsiIfStatement) TranspileUtil.createStatementFromText(text);
//                ifStmt = (PsiIfStatement) replace(stmt, ifStmt);
//                ifStmt = (PsiIfStatement) dest.add(ifStmt);
//                dest = ((PsiBlockStatement) NncUtils.requireNonNull(ifStmt.getThenBranch())).getCodeBlock();
//                dest.add(stmt.copy());
//            } else {
//                dest.add(stmt);
//            }
//        }
//        return result;
//    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        nameTracker.enterBlock();
        super.visitCodeBlock(block);
        nameTracker.exitBlock();
    }

    private static class BlockInfo {
        private final boolean isLoop;
        private final @Nullable String label;
        private final @Nullable BlockInfo parent;
        private final Set<String> usedContinues = new HashSet<>();
        private boolean shouldWrapNext;
        private boolean shouldWrapCurrent;

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
        private final @Nullable String label;
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

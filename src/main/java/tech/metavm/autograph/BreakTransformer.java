package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.expression.CodeBlock;
import tech.metavm.util.InternalException;
import tech.metavm.util.KeyValue;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.autograph.TranspileUtil.createStatementFromText;
import static tech.metavm.util.NncUtils.requireNonNull;

public class BreakTransformer extends VisitorBase {

    private Section section;
    private final NameTracker nameTracker = new NameTracker();
    private BlockInfo block;
    private AbsBlockStmt blockStmt;
    //    private final List<PsiCodeBlock> copyDestinations = new ArrayList<>();
    private final List<BreakSite> breakSites = new ArrayList<>();
    private final List<Destination> destinations = new ArrayList<>();
    private final LinkedList<BranchStmt> branches = new LinkedList<>();

    private void defineBreakVar(PsiStatement statement) {
        var loop = currentLoopInfo();
        var breakVarDecl = createStatementFromText("boolean " + loop.breakVar + " = false;");
        insertBefore(breakVarDecl, statement);
    }

    private List<PsiElement> getGeneratedElements(PsiElement element) {
        List<PsiElement> result = new ArrayList<>();
        getGeneratedElements(element, result);
        return result;
    }

    private void getGeneratedElements(PsiElement element, List<PsiElement> result) {
        for (PsiElement insert : getInsertsBefore(element)) {
            getGeneratedElements(insert, result);
        }
        result.add(element);
    }

    @Override
    public void visitStatement(PsiStatement statement) {
        super.visitStatement(statement);
        List<PsiElement> generated = getGeneratedElements(statement);
//        if(!copyDestinations.isEmpty()) {
        for (BreakSite breakSite : breakSites) {
            if (isSuccessor(breakSite.statement, statement)) {
                for (PsiCodeBlock dest : breakSite.relocations) {
                    for (PsiElement rep : generated) {
                        dest.add(rep);
                    }
                }
                for (PsiElement element : generated) {
                    element.delete();
                }
            }
        }
//        }
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        var ifStmt = enterIfStmt(statement);
        if (statement.getThenBranch() != null) {
            enterBranch(ifStmt.thenBranch);
            ifStmt.inThenBranch = true;
            visitNonLoopBody(statement.getThenBranch());
            exitBranch();
        }
        if (statement.getElseBranch() != null) {
            enterBranch(ifStmt.elseBranch);
            ifStmt.inThenBranch = false;
            visitNonLoopBody(statement.getElseBranch());
            exitBranch();
        }
        exitIfStmt();
        NncUtils.requireFalse(ifStmt.thenBranch.isDest && ifStmt.elseBranch.isDest,
                "Both branches are copy destinations");
        for (BranchStmt branchStmt : List.of(ifStmt.thenBranch, ifStmt.elseBranch)) {
            if(branchStmt.isDest) {
                ensureBlockBranch(statement, branchStmt.isThen);
                var branch = branchStmt.isThen ? statement.getThenBranch() :
                        statement.getElseBranch();
                var breakStmt = branchStmt.breakStmt;
                var block = ((PsiBlockStatement) requireNonNull(branch)).getCodeBlock();
                var parentLimit = getParentLimit(breakStmt);
                var start = getGlobalSuccessor(statement, parentLimit);
                var next = start;
                while (next != null) {
                    block.add(next);
                    next = getGlobalSuccessor(next, parentLimit);
                }
                visitChain(start);
            }
        }
    }

    private PsiStatement getParentLimit(BreakStmt breakStmt) {
        var current = breakStmt.statement.getParent();
        while (current != breakStmt.target) {
            if(current instanceof PsiSwitchLabeledRuleStatement || current instanceof PsiLoopStatement) {
                return (PsiStatement) current;
            }
            current = current.getParent();
        }
        return breakStmt.target;
    }

    private void enterBranch(BranchStmt branch) {
        branches.push(branch);
    }

    private @Nullable BranchStmt currentBranch() {
        return branches.peek();
    }

    private void exitBranch() {
        branches.pop();
    }

    private @Nullable IfStmt currentIfStmt() {
        var current = blockStmt;
        while (current != null && !(current instanceof IfStmt)) {
            current = current.parent;
        }
        return (IfStmt) current;
    }

    private AbsBlockStmt currentBlockStmt() {
        return NncUtils.requireNonNull(blockStmt);
    }

    private IfStmt enterIfStmt(PsiIfStatement ifStatement) {
        var ifBlockStmt = new IfStmt(ifStatement, currentBlock(), blockStmt, currentBranch());
        blockStmt = ifBlockStmt;
        return ifBlockStmt;
    }

    private void exitIfStmt() {
//        var ifBlock = (IfStmt) currentBlockStmt();
//        var ifStmt = (PsiIfStatement) ifBlock.statement;
//        if(ifStmt.getThenBranch() instanceof PsiBlockStatement thenBlockStmt) {
//            copyDestinations.remove(thenBlockStmt.getCodeBlock());
//        }
//        if(ifStmt.getElseBranch() instanceof PsiBlockStatement elseBlockStmt) {
//            copyDestinations.remove(elseBlockStmt.getCodeBlock());
//        }
        exitBlockStmt();
    }

    private void enterLoopStmt(PsiLoopStatement loopStatement) {
        blockStmt = new LoopStmt(loopStatement, currentBlock(), blockStmt);
    }

    private void exitLoopStmt() {
        exitBlockStmt();
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterLoop(statement);
        var body = statement.getBody();
        if (body != null) {
            var block = visitLoopBody(statement.getBody(), getLabel(statement));
            if (block.breakUsed()) {
                var firstStmt = getFirstStatement(statement.getBody());
                if (isExtraLoopTest(firstStmt)) {
                    PsiMethodCallExpression methodCallExpr =
                            (PsiMethodCallExpression) ((PsiExpressionStatement) firstStmt).getExpression();
                    String cond = "(" + methodCallExpr.getArgumentList().getExpressions()[0].getText()
                            + ") && (" + block.getConditionForUsed() + ")";
                    String text = EXTRA_LOOP_TEST + "(" + cond + ");";
                    var extraLoopTest = TranspileUtil.createStatementFromText(text);
                    firstStmt.replace(extraLoopTest);
                } else {
                    String text = EXTRA_LOOP_TEST + "(" + block.getConditionForUsed() + ");";
                    var extraLoopTest = TranspileUtil.createStatementFromText(text);
                    prependBody(statement.getBody(), extraLoopTest);
                }
            }
        }
        exitLoop();
    }

    private BlockInfo visitLoopBody(PsiElement body, @Nullable String label) {
        if (body instanceof PsiCodeBlock) {
            nameTracker.enterBlock();
        }
        var block = enterBlock(true, label);
//        visitBlock(body);
        body.accept(this);
        var replacement = getReplacement(body);
        if (section.breakUsed) {
            String code = "boolean " + section.breakVar + " = false;";
            var toInsert = TranspileUtil.createStatementFromText(code);
            insertBefore(toInsert, (PsiStatement) replacement.getParent());
        }
        exitBlock();
        if (body instanceof PsiCodeBlock) {
            nameTracker.exitBlock();
        }
        return block;
    }

    private void visitNonLoopBody(PsiStatement body) {
        if (body instanceof PsiCodeBlock) {
            nameTracker.enterBlock();
        }
        enterBlock(false, null);
//        visitBlock(body);
        body.accept(this);
        exitBlock();
        if (body instanceof PsiCodeBlock) {
            nameTracker.exitBlock();
        }
    }

    private void addDestination(PsiCodeBlock block) {

    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterLoop(statement);
        if (statement.getBody() != null) {
            var block = visitLoopBody(statement.getBody(), getLabel(statement));
            if (block.breakUsed()) {
                var cond = TranspileUtil.createExpressionFromText(block.getConditionForUsed());
                var currentCond = Objects.requireNonNull(statement.getCondition());
                currentCond.replace(TranspileUtil.and(currentCond, cond));
            }
        }
        exitLoop();
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        enterLoop(statement);
        if (statement.getBody() != null) {
            var block = visitLoopBody(statement.getBody(), getLabel(statement));
            if (block.breakUsed()) {
                var cond = TranspileUtil.createExpressionFromText(block.getConditionForUsed());
                TranspileUtil.replaceForCondition(statement, cond);
            }
        }
        exitLoop();
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        var section = enterSection(statement);
        if (statement.getBody() != null) {
            var fistStmt = statement.getBody().getStatements()[0];
            if (fistStmt instanceof PsiSwitchLabeledRuleStatement) {
                var stmts = statement.getBody().getStatements();
                for (PsiStatement stmt : stmts) {
                    var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
                    if (labeledRuleStmt.getBody() != null) {
                        visitNonLoopBody(labeledRuleStmt.getBody());
                    }
                }
            } else {

            }
        }
        if (section.breakUsed) {
            defineBreakVar(statement);
        }
        exitSection();
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        if (statement.getBody() != null) {
            visitNonLoopBody(statement.getBody());
        }
    }

    private Section currentLoopInfo() {
        return requireNonNull(section);
    }

    private BlockInfo currentBlock() {
        return requireNonNull(block);
    }

    private BlockInfo enterBlock(boolean isLoop, @Nullable String label) {
        return block = new BlockInfo(isLoop, label, block);
    }

    private void exitBlock() {
        block = currentBlock().parent;
    }

    private void enterBlockStmt(PsiBlockStatement statement) {
        blockStmt = new BlockStmt(statement, currentBlock(), blockStmt);
    }

    private void exitBlockStmt() {
        blockStmt = blockStmt.parent;
    }

    private Section enterLoop(PsiLoopStatement statement) {
        enterLoopStmt(statement);
        return enterSection(statement);
    }

    private void exitLoop() {
        exitSection();
        ;
        exitLoopStmt();
    }

    private Section enterSection(PsiElement element) {
        return section = new Section(element, section, nameTracker.nextName("_break"));
    }

    private void exitSection() {
        section = currentLoopInfo().parent;
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        nameTracker.enterBlock();
        super.visitCodeBlock(block);
        nameTracker.exitBlock();
    }

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        enterBlockStmt(statement);
        super.visitBlockStatement(statement);
        exitBlockStmt();
    }

    private PsiBlockStatement ensureBlockBranch(PsiIfStatement ifStmt, boolean thenBranch) {
        PsiStatement branch = thenBranch ? ifStmt.getThenBranch() : ifStmt.getElseBranch();
        if (branch instanceof PsiBlockStatement branchStmt) {
            return branchStmt;
        } else {
            PsiStatement oldBranch = branch;
            var branchBlockStmt = (PsiBlockStatement) TranspileUtil.createStatementFromText("{}");
            if (thenBranch) {
                ifStmt.setThenBranch(branchBlockStmt);
            } else {
                ifStmt.setElseBranch(branchBlockStmt);
            }
            branchBlockStmt = (PsiBlockStatement) requireNonNull(
                    thenBranch ? ifStmt.getThenBranch() : ifStmt.getElseBranch());
            var branchBlock = branchBlockStmt.getCodeBlock();
            if (oldBranch != null) {
                branchBlock.add(oldBranch.copy());
            }
            return branchBlockStmt;
        }
    }

    protected void visitBlock(PsiElement body) {
        var ref = new Object() {
            boolean breakUsed;
            String cond;
        };
        visitBlock(body,
                stmt -> {
                    var block = currentBlock();
                    ref.breakUsed = block.isCheckRequired();
                    ref.cond = block.getConditionForUnchecked();
                    block.shouldWrapCurrent = block.shouldWrapNext;
                    block.shouldWrapNext = false;
                    block.clearUsedBreaks();
                },
                stmt -> {
                    var block = currentBlock();
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
                    } else if (ref.breakUsed) {
                        var ifStmt = (PsiIfStatement) createStatementFromText("if (" + ref.cond + ") {}");
                        ifStmt = (PsiIfStatement) replace(stmt, ifStmt);
                        var newDest = ((PsiBlockStatement) requireNonNull(ifStmt.getThenBranch())).getCodeBlock();
                        newDest.add(stmt.copy());
                        return new KeyValue<>(ifStmt, newDest);
                    } else {
                        return new KeyValue<>(stmt, null);
                    }
                }
        );
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        String label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var loop = currentLoopInfo();
        if (label != null) {
            while (loop != null && !Objects.equals(loop.label, label)) {
                loop = loop.parent;
            }
            requireNonNull(loop, "Can not find an enclosing loop with label '" + label + "'");
        }
        var target =  getBreakTarget(statement);
        String text = loop.breakVar + " = true;";
        var replacement = (PsiStatement) replace(statement, createStatementFromText(text));
        var breakStmt = new BreakStmt(target, replacement);
        for (BranchStmt branchStmt : branches) {
            branchStmt.useBreak(breakStmt);
        }
        loop.breakUsed = true;
        block.useBreak(loop.breakVar, label);
    }

    private PsiStatement getBreakTarget(PsiBreakStatement breakStatement) {
        var current = breakStatement.getParent();
        var label = NncUtils.get(breakStatement.getLabelIdentifier(), PsiElement::getText);
        while (current != null) {
            if(breakStatement.getLabelIdentifier() != null) {
                if(Objects.equals(getLabel(current), label)) {
                    return (PsiStatement) current;
                }
            }
            else if(isLoopStatement(current) || current instanceof PsiSwitchStatement) {
                return (PsiStatement) current;
            }
            current = current.getParent();
        }
        throw new InternalException("Can not find the target of break statement: " + breakStatement);
    }

    private boolean isLoopStatement(PsiElement element) {
        return element instanceof PsiForStatement || element instanceof PsiWhileStatement
                || element instanceof PsiForeachStatement;
    }

    private boolean matchLabel(PsiIdentifier label, PsiElement element) {
        if (label == null) {
            return false;
        }
        return Objects.equals(label.getText(), getLabel(element));
    }

    private boolean isBreakTarget(PsiBreakStatement breakStatement, PsiElement target) {
        if (breakStatement.getLabelIdentifier() != null) {
            return breakStatement.getLabelIdentifier().getText().equals(getLabel(target));
        } else {
            return target instanceof PsiWhileStatement || target instanceof PsiForeachStatement
                    || target instanceof PsiForStatement;
        }
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
    public void visitMethod(PsiMethod method) {
        nameTracker.enterMethod();
        super.visitMethod(method);
        nameTracker.exitMethod();
    }

    private static class Section {

        @Nullable
        final Section parent;
        final PsiElement element;
        final String label;
        boolean breakUsed;
        final String breakVar;

        private Section(PsiElement element, @Nullable Section parent, String breakVar) {
            this.element = element;
            this.parent = parent;
            if (element.getParent() instanceof PsiLabeledStatement labeledStatement) {
                label = labeledStatement.getLabelIdentifier().getText();
            } else {
                label = null;
            }
            this.breakVar = breakVar;
        }

        List<Section> allEnclosingLoops() {
            List<Section> loops = new ArrayList<>();
            Section loop = this;
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

    private static class BreakSite {
        final PsiBreakStatement statement;
        final List<PsiCodeBlock> relocations;

        private BreakSite(PsiBreakStatement statement, List<PsiCodeBlock> relocations) {
            this.statement = statement;
            this.relocations = relocations;
        }
    }

    private static class Destination {
        private final PsiCodeBlock bock;

        private Destination(PsiCodeBlock bock) {
            this.bock = bock;
        }
    }

    private static final class BranchStmt {
        private final PsiIfStatement ifStatement;
        private final boolean isThen;
        private final PsiStatement statement;
        private boolean marked;
        private BranchStmt oppositeBranch;
        private boolean breakUsed;
        private BreakStmt breakStmt;
        private @Nullable BranchStmt enclosingBranch;
        private boolean isDest;

        private BranchStmt(PsiIfStatement ifStatement,
                           boolean isThen,
                           @Nullable BranchStmt enclosingBranch) {
            this.ifStatement = ifStatement;
            this.isThen = isThen;
            this.statement = isThen ? ifStatement.getThenBranch() : ifStatement.getElseBranch();
            this.enclosingBranch = enclosingBranch;
        }

        private void useBreak(BreakStmt breakStmt) {
            this.breakStmt = breakStmt;
            breakUsed = true;
            oppositeBranch.makeDest(breakStmt);
        }

        void makeDest(BreakStmt breakStmt) {
            this.breakStmt = breakStmt;
            isDest = true;
            var current = enclosingBranch;
            while (current != null) {
                current.unmarkDest();
                current = current.enclosingBranch;
            }
        }

        void unmarkDest() {
            isDest = false;
        }

    }

    private record BreakStmt(PsiStatement target, PsiStatement statement) {
    }


    private static boolean isSuccessor(PsiStatement statement, PsiStatement successor) {
        var next = getGlobalSuccessor(statement);
        while (next != null) {
            if (next == successor) {
                return true;
            }
            next = getGlobalSuccessor(next);
        }
        return false;
    }

    private static PsiStatement getGlobalSuccessor(PsiStatement statement) {
        return getGlobalSuccessor(statement, null);
    }

    private static @Nullable PsiStatement getGlobalSuccessor(PsiStatement statement, @Nullable PsiStatement parentLimit) {
        PsiElement next = statement.getNextSibling();
        while (next != null && !(next instanceof PsiStatement)) {
            next = next.getNextSibling();
        }
        if (next != null) {
            return (PsiStatement) next;
        }
        var parent = statement.getParent();
        if(parent == null) {
            return null;
        }
        var enclosingStmt = requireNonNull(
                TranspileUtil.getAncestor(parent, PsiStatement.class, PsiMethod.class, PsiLambdaExpression.class)
        );
        if(enclosingStmt == parentLimit) {
            return null;
        }
        if (enclosingStmt instanceof PsiStatement parentStmt) {
            return getGlobalSuccessor(parentStmt, parentLimit);
        } else {
            return null;
        }
    }

    private static class AbsBlockStmt {
        final PsiStatement statement;
        final BlockInfo containingBlock;
        final AbsBlockStmt parent;

        private AbsBlockStmt(PsiStatement statement, BlockInfo containingBlock, AbsBlockStmt parent) {
            this.statement = statement;
            this.containingBlock = containingBlock;
            this.parent = parent;
        }
    }

    private static class BlockStmt extends AbsBlockStmt {

        private BlockStmt(PsiBlockStatement statement, BlockInfo containingBlock, AbsBlockStmt parent) {
            super(statement, containingBlock, parent);
        }
    }

    private static class LoopStmt extends AbsBlockStmt {

        private LoopStmt(PsiLoopStatement statement, BlockInfo containingBlock, AbsBlockStmt parent) {
            super(statement, containingBlock, parent);
        }

    }

    private static class IfStmt extends AbsBlockStmt {
        boolean inThenBranch;
        boolean breakUsed;
        boolean breakAtThenBranch;
        final BranchStmt thenBranch;
        final BranchStmt elseBranch;
        final @Nullable IfStmt enclosingIf;

        private IfStmt(PsiIfStatement statement,
                       BlockInfo containingBlock,
                       AbsBlockStmt parent,
                       @Nullable BranchStmt enclosingBranch
                       ) {
            super(statement, containingBlock, parent);
            thenBranch = new BranchStmt(statement, true, enclosingBranch);
            elseBranch = new BranchStmt(statement, false, enclosingBranch);
            thenBranch.oppositeBranch = elseBranch;
            elseBranch.oppositeBranch = thenBranch;
            var current = parent;
            while (current != null && !(current instanceof IfStmt)) {
                current = current.parent;
            }
            enclosingIf = (IfStmt) current;
        }
    }

    private static class BlockInfo {
        private final boolean isLoop;
        private final @Nullable String label;
        private final @Nullable BlockInfo parent;
        private CodeBlock dest;
        private final Set<String> uncheckedBreaks = new HashSet<>();
        private final Set<String> usedBreaks = new HashSet<>();
        private boolean shouldWrapCurrent;
        private boolean shouldWrapNext;
        private PsiElement wrapElement;

        private BlockInfo(boolean isLoop, @Nullable String label, @Nullable BlockInfo parent) {
            this.isLoop = isLoop;
            this.label = label;
            this.parent = parent;
        }

        void useBreak(String variable, @Nullable String label) {
            BlockInfo block = this;
            while (block != null) {
                block.usedBreaks.add(variable);
                block.uncheckedBreaks.add(variable);
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

        boolean isCheckRequired() {
            return !uncheckedBreaks.isEmpty();
        }

        boolean breakUsed() {
            return !usedBreaks.isEmpty();
        }

        void clearUsedBreaks() {
            uncheckedBreaks.clear();
        }

        String getConditionForUnchecked() {
            return getConditionText(uncheckedBreaks);
        }

        String getConditionForUsed() {
            return getConditionText(usedBreaks);
        }

        String getConditionText(Set<String> vars) {
            return NncUtils.join(vars, var -> "!" + var, " && ");
        }

    }

}



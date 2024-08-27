package org.metavm.flow;

import org.metavm.expression.*;
import org.metavm.util.LinkedList;
import org.metavm.util.NncUtils;

import java.util.*;

public class FlowAnalyzer extends VoidStructuralVisitor {

    private final LinkedList<BranchNode> branchNodes = new LinkedList<>();
    private final Map<BranchNode, CondSection> condSections = new HashMap<>();

    @Override
    public Void visitNode(NodeRT node) {
        if(node.getPredecessor() != null)
            node.mergeExpressionTypes(node.getPredecessor().getExpressionTypes());
        else
            node.mergeExpressionTypes(node.getScope().getExpressionTypes());
        return super.visitNode(node);
    }

    @Override
    public Void visitMergeNode(MergeNode node) {
        node.mergeExpressionTypes(MergeNode.getExpressionTypeMap(node.getBranchNode()));
        return super.visitMergeNode(node);
    }

    @Override
    public Void visitCheckNode(CheckNode node) {
        var narrower = new TypeNarrower(node.getExpressionTypes()::getType);
        var condition = node.getCondition().getExpression();
        node.mergeExpressionTypes(narrower.narrowType(condition));
        var exitExprTypes = node.getExpressionTypes().merge(narrower.narrowType(Expressions.not(condition)));
        addBranchEntry(exitExprTypes, node.getExit());
        return super.visitCheckNode(node);
    }

    @Override
    public Void visitLoopNode(LoopNode node) {
        TypeNarrower narrower = new TypeNarrower(node.getExpressionTypes()::getType);
        node.getScope().setExpressionTypes(narrower.narrowType(node.getCondition().getExpression()));
        return super.visitLoopNode(node);
    }

    @Override
    public Void visitBranchNode(BranchNode node) {
        enterCondSection(node);
        var result =  super.visitBranchNode(node);
        exitCondSection();
        return result;
    }

    @Override
    public Void visitBranch(Branch branch) {
        var condSection = getCondSection(branch.getOwner());
        branch.getScope().setExpressionTypes(condSection.nextBranch(branch));
        return super.visitBranch(branch);
    }

    @Override
    public Void visitScope(ScopeRT scope) {
        if(scope.getOwner() != null)
            scope.mergeExpressionTypes(scope.getOwner().getExpressionTypes());
        return super.visitScope(scope);
    }

    private void addBranchEntry(ExpressionTypeMap entry, BranchNode branchNode) {
        var section = NncUtils.requireNonNull(condSections.get(branchNode));
        section.nextBranchEntries.add(entry);
    }

    private void enterCondSection(BranchNode branchNode) {
        branchNodes.push(branchNode);
        condSections.put(branchNode, new CondSection(branchNode));
    }

    private void exitCondSection() {
        var branchNode = Objects.requireNonNull(branchNodes.pop());
        condSections.remove(branchNode);
    }

    private CondSection getCondSection(BranchNode secondId) {
        return Objects.requireNonNull(condSections.get(secondId), () -> "Cannot find CondSection for " + secondId);
    }

    private static class CondSection {
        private final BranchNode branchNode;
        private Expression nextBranchCond;
        private final List<ExpressionTypeMap> nextBranchEntries = new ArrayList<>();

        private CondSection(BranchNode branchNode) {
            this.branchNode = branchNode;
        }

        public ExpressionTypeMap nextBranch(Branch branch) {
            var narrower = new TypeNarrower(branchNode.getExpressionTypes()::getType);
            ExpressionTypeMap extraExprTypeMap = null;
            if(nextBranchCond != null && !Expressions.isConstantFalse(nextBranchCond)) {
                extraExprTypeMap = narrower.narrowType(nextBranchCond);
            }
            for (var entry : nextBranchEntries) {
                if(extraExprTypeMap == null) {
                    extraExprTypeMap = entry;
                }
                else {
                    extraExprTypeMap = entry.union(extraExprTypeMap);
                }
            }
            var exprTypeMap = narrower.narrowType(branch.getCondition().getExpression());
            nextBranchEntries.clear();
            nextBranchCond = nextBranchCond == null ?
                    Expressions.not(branch.getCondition().getExpression()) :
                    Expressions.and(nextBranchCond, Expressions.not(branch.getCondition().getExpression()));
            return extraExprTypeMap != null ? exprTypeMap.merge(extraExprTypeMap) : exprTypeMap;
        }

    }

}

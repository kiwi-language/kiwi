package tech.metavm.flow;

import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.expression.TypeNarrower;
import tech.metavm.expression.VoidStructuralVisitor;

public class FlowAnalyzer extends VoidStructuralVisitor {

    @Override
    public Void visitNode(NodeRT node) {
        if(node.getPredecessor() != null) {
            node.setExpressionTypes(node.getPredecessor().getExpressionTypes());
        }
        else {
            node.setExpressionTypes(node.getScope().getExpressionTypes());
        }
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
        node.mergeExpressionTypes(narrower.narrowType(node.getCondition().getExpression()));
        return super.visitCheckNode(node);
    }

    @Override
    public Void visitScope(ScopeRT scope) {
        scope.setExpressionTypes(ExpressionTypeMap.EMPTY);
        if(scope.getOwner() != null) {
            scope.mergeExpressionTypes(scope.getOwner().getExpressionTypes());
        }
        TypeNarrower narrower = new TypeNarrower(scope.getExpressionTypes()::getType);
        if(scope.getOwner() instanceof LoopNode loopNode) {
            scope.mergeExpressionTypes(narrower.narrowType(loopNode.getCondition().getExpression()));
        }
        else if(scope.getBranch() != null) {
            scope.mergeExpressionTypes(narrower.narrowType(scope.getBranch().getCondition().getExpression()));
        }
        return super.visitScope(scope);
    }
}

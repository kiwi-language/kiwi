package tech.metavm.flow;

import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.autograph.TypeNarrower;
import tech.metavm.expression.ElementVisitor;

public class FlowAnalyzer extends ElementVisitor {

    @Override
    public void visitNode(NodeRT<?> node) {
        if(node.getPredecessor() != null) {
            node.setExpressionTypes(node.getPredecessor().getExpressionTypes());
        }
        else {
            node.setExpressionTypes(node.getScope().getExpressionTypes());
        }
        super.visitNode(node);
    }

//    @Override
//    public void visitMergeNode(MergeNode node) {
//        var exprTypes = MergeNode.getExpressionTypeMap(node.getBranchNode());
//        node.mergeExpressionTypes(exprTypes);
//    }

    @Override
    public void visitCheckNode(CheckNode node) {
        var narrower = new TypeNarrower(node.getExpressionTypes()::getType);
        node.mergeExpressionTypes(narrower.narrowType(node.getCondition().getExpression()));
    }

    @Override
    public void visitScope(ScopeRT scope) {
        scope.setExpressionTypes(ExpressionTypeMap.EMPTY);
        if(scope.getOwner() != null) {
            scope.mergeExpressionTypes(scope.getOwner().getExpressionTypes());
        }
        TypeNarrower narrower = new TypeNarrower(scope.getExpressionTypes()::getType);
        if(scope.getOwner() instanceof LoopNode<?> loopNode) {
            scope.mergeExpressionTypes(narrower.narrowType(loopNode.getCondition().getExpression()));
        }
        else if(scope.getBranch() != null) {
            scope.mergeExpressionTypes(narrower.narrowType(scope.getBranch().getCondition().getExpression()));
        }
        super.visitScope(scope);
    }
}

package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.expression.TypeNarrower;
import org.metavm.expression.VoidStructuralVisitor;

@Slf4j
public class FlowAnalyzer extends VoidStructuralVisitor {

    @Override
    public Void visitNode(NodeRT node) {
        NodeRT prev = node.getPredecessor();
        if(prev != null && prev.isSequential())
            node.mergeExpressionTypes(prev.getNextExpressionTypes());
        return super.visitNode(node);
    }

    @Override
    public Void visitIfNode(IfNode node) {
        var narrower = new TypeNarrower(node.getExpressionTypes()::getType);
        var condition = node.getCondition().getExpression();
        var targetExprTypes = node.getExpressionTypes().merge(narrower.narrowType(condition));
        node.getTarget().unionExpressionTypes(targetExprTypes);
        return super.visitIfNode(node);
    }

    @Override
    public Void visitScope(ScopeRT scope) {
        if(scope.getOwner() != null)
            scope.mergeExpressionTypes(scope.getOwner().getExpressionTypes());
        return super.visitScope(scope);
    }

}

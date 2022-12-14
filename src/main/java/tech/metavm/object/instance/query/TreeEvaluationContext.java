package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

import java.util.Set;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectTree objectTree;

    public TreeEvaluationContext(ObjectTree objectTree) {
        this.objectTree = objectTree;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof FieldExpression fieldExpression) {
            return objectTree.getFieldValue(fieldExpression.getPathString());
        }
        else {
            throw new RuntimeException("Unsupported expression");
        }
    }

    @Override
    public Set<Class<? extends Expression>> supportedExpressionClasses() {
        return Set.of(FieldExpression.class);
    }
}

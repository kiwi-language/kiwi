package tech.metavm.expression;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.query.ObjectNode;
import tech.metavm.object.instance.query.PathResolver;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectNode objectNode;
    private final Instance instance;

    public TreeEvaluationContext(ObjectNode objectNode, Instance instance) {
        this.objectNode = objectNode;
        this.instance = instance;
    }

    @Override
    public Instance evaluate(Expression expression) {
        if(expression instanceof PropertyExpression fieldExpression)
            return objectNode.getByPath(instance, PathResolver.getFieldPath(fieldExpression));
        else
            throw new RuntimeException("Unsupported expression");
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof PropertyExpression;
    }

}

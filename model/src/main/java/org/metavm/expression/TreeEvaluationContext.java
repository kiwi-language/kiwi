package org.metavm.expression;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.query.ObjectNode;
import org.metavm.object.instance.query.PathResolver;

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

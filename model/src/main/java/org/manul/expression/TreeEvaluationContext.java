package org.manul.expression;

import org.manul.object.instance.core.Value;
import org.manul.object.instance.query.ObjectNode;
import org.manul.object.instance.query.PathResolver;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectNode objectNode;
    private final Value instance;

    public TreeEvaluationContext(ObjectNode objectNode, Value instance) {
        this.objectNode = objectNode;
        this.instance = instance;
    }

    @Override
    public Value evaluate(Expression expression) {
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

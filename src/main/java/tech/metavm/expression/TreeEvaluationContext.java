package tech.metavm.expression;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.ObjectNode;
import tech.metavm.object.instance.query.Path;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectNode objectNode;
    private final Instance instance;

    public TreeEvaluationContext(ObjectNode objectNode, Instance instance) {
        this.objectNode = objectNode;
        this.instance = instance;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof FieldExpression fieldExpression) {
            return objectNode.getByPath(instance, Path.create(fieldExpression.getPathString()));
        }
        else {
            throw new RuntimeException("Unsupported expression");
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof FieldExpression;
    }

}

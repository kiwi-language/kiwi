package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.query.ObjectNode;
import tech.metavm.object.instance.query.Path;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectNode objectNode;
    private final Instance instance;
    private final IEntityContext entityContext;

    public TreeEvaluationContext(ObjectNode objectNode, Instance instance, IEntityContext entityContext) {
        this.objectNode = objectNode;
        this.instance = instance;
        this.entityContext = entityContext;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof PropertyExpression fieldExpression) {
            return objectNode.getByPath(instance, Path.create(fieldExpression.getProperty().getName()));
        }
        else {
            throw new RuntimeException("Unsupported expression");
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof PropertyExpression;
    }

    @Override
    public IEntityContext getEntityContext() {
        return entityContext;
    }

}

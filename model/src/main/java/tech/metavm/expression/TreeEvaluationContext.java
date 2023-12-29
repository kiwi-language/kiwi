package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.query.ObjectNode;
import tech.metavm.object.instance.query.Path;

public class TreeEvaluationContext implements EvaluationContext {

    private final ObjectNode objectNode;
    private final Instance instance;
    private final ParameterizedFlowProvider parameterizedFlowProvider;

    public TreeEvaluationContext(ObjectNode objectNode, Instance instance, ParameterizedFlowProvider parameterizedFlowProvider) {
        this.objectNode = objectNode;
        this.instance = instance;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
    }

    @Override
    public Instance evaluate(Expression expression) {
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
    public ParameterizedFlowProvider getParameterizedFlowProvider() {
        return parameterizedFlowProvider;
    }

}

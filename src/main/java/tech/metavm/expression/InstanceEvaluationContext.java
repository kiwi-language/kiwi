package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.InternalException;

public class InstanceEvaluationContext implements EvaluationContext {

    private final ClassInstance instance;
    private final IEntityContext entityContext;

    public InstanceEvaluationContext(ClassInstance instance, IEntityContext entityContext) {
        this.instance = instance;
        this.entityContext = entityContext;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(isContextExpression(expression)) {
            return instance;
        }
        else {
            throw new InternalException(evaluator + " is not a context expression of " + this);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof ThisExpression;
    }

    @Override
    public IEntityContext getEntityContext() {
        return entityContext;
    }

}

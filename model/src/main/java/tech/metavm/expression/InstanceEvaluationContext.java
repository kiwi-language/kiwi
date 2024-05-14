package tech.metavm.expression;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.InternalException;

public class InstanceEvaluationContext implements EvaluationContext {

    private final ClassInstance instance;

    public InstanceEvaluationContext(ClassInstance instance) {
        this.instance = instance;
    }

    @Override
    public Instance evaluate(Expression expression) {
        if(isContextExpression(expression))
            return instance;
        else
            throw new InternalException(expression + " is not a context expression of " + this);
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof ThisExpression;
    }


}

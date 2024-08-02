package org.metavm.expression;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.InternalException;

public class InstanceEvaluationContext implements EvaluationContext {

    private final ClassInstance instance;

    public InstanceEvaluationContext(ClassInstance instance) {
        this.instance = instance;
    }

    @Override
    public Value evaluate(Expression expression) {
        if(isContextExpression(expression))
            return instance.getReference();
        else
            throw new InternalException(expression + " is not a context expression of " + this);
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof ThisExpression;
    }


}

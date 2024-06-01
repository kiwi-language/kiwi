package tech.metavm.expression;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

public class SubEvaluationContext implements EvaluationContext {

    private final EvaluationContext parent;
    private final Instance instance;

    public SubEvaluationContext(EvaluationContext parent, Instance instance) {
        this.parent = parent;
        this.instance = instance;
    }

    @Override
    @Nullable
    public Instance evaluate(Expression expression) {
        if (isSelfContextExpression(expression))
            return instance;
        else if (parent != null && this.parent.isContextExpression(expression))
            return parent.evaluate(expression);
        else
            throw new InternalException(expression + " is not a context expression of " + this);
    }

    private boolean isSelfContextExpression(Expression expression) {
        return expression instanceof CursorExpression;
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return isSelfContextExpression(expression) || this.parent.isContextExpression(expression);
    }

}

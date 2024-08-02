package org.metavm.expression;

import org.metavm.object.instance.core.Value;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;

public class SubEvaluationContext implements EvaluationContext {

    private final EvaluationContext parent;
    private final Value instance;

    public SubEvaluationContext(EvaluationContext parent, Value instance) {
        this.parent = parent;
        this.instance = instance;
    }

    @Override
    @Nullable
    public Value evaluate(Expression expression) {
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

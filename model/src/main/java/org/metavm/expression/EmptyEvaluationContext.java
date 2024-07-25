package org.metavm.expression;

import org.metavm.object.instance.core.Instance;

import javax.annotation.Nullable;

public class EmptyEvaluationContext implements EvaluationContext{
    @Nullable
    @Override
    public Instance evaluate(Expression expression) {
        return null;
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return false;
    }
}

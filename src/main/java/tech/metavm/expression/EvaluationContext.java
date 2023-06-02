package tech.metavm.expression;

import tech.metavm.object.instance.Instance;

import javax.annotation.Nullable;

public interface EvaluationContext {

    @Nullable
    Instance evaluate(Expression expression, ExpressionEvaluator evaluator);

    boolean isContextExpression(Expression expression);

}

package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

import java.util.Set;

public interface EvaluationContext {

    Instance evaluate(Expression expression, ExpressionEvaluator evaluator);

    Set<Class<? extends Expression>> supportedExpressionClasses();

    default boolean isExpressionSupported(Class<? extends Expression> expressionClass) {
        return supportedExpressionClasses().contains(expressionClass);
    }

}

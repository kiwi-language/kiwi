package tech.metavm.object.instance.query;

import java.util.Set;

public interface EvaluationContext {

    Object evaluate(Expression expression, ExpressionEvaluator evaluator);

    Set<Class<? extends Expression>> supportedExpressionClasses();

    default boolean isExpressionSupported(Class<? extends Expression> expressionClass) {
        return supportedExpressionClasses().contains(expressionClass);
    }

}

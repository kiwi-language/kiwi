package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

import javax.annotation.Nullable;
import java.util.Set;

public interface EvaluationContext {

    @Nullable
    Instance evaluate(Expression expression, ExpressionEvaluator evaluator);

    boolean isContextExpression(Expression expression);

}

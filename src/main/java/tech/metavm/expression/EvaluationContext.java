package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.generic.MetaSubstitutor;

import javax.annotation.Nullable;

public interface EvaluationContext {

    @Nullable
    Instance evaluate(Expression expression, ExpressionEvaluator evaluator);

    boolean isContextExpression(Expression expression);

    IEntityContext getEntityContext();

    default @Nullable MetaSubstitutor getSubstitutor() {
        return null;
    }

}

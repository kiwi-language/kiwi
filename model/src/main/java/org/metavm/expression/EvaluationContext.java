package org.metavm.expression;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.generic.MetaSubstitutor;

import javax.annotation.Nullable;

public interface EvaluationContext {

    @Nullable
    Instance evaluate(Expression expression);

    boolean isContextExpression(Expression expression);

    default @Nullable MetaSubstitutor getSubstitutor() {
        return null;
    }

}

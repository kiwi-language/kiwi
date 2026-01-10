package org.manul.expression;

import org.manul.object.instance.core.Value;
import org.manul.object.type.generic.MetaSubstitutor;

import javax.annotation.Nullable;

public interface EvaluationContext {

    @Nullable
    Value evaluate(Expression expression);

    boolean isContextExpression(Expression expression);

    default @Nullable MetaSubstitutor getSubstitutor() {
        return null;
    }

}

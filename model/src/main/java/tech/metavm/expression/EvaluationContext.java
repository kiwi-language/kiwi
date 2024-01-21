package tech.metavm.expression;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.generic.MetaSubstitutor;

import javax.annotation.Nullable;

public interface EvaluationContext {

    @Nullable
    Instance evaluate(Expression expression);

    boolean isContextExpression(Expression expression);

    ParameterizedFlowProvider parameterizedFlowProvider();

    default @Nullable MetaSubstitutor getSubstitutor() {
        return null;
    }

}

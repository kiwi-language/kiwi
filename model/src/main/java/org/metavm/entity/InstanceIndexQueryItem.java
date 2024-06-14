package org.metavm.entity;

import org.metavm.expression.EvaluationContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.IndexField;

public record InstanceIndexQueryItem(
        IndexField field,
        IndexOperator operator,
        Instance value
) {
    public boolean matches(EvaluationContext evaluationContext) {
        var actualValue = field.getValue().evaluate(evaluationContext);
        return operator.evaluate(actualValue, value);
    }
}

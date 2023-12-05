package tech.metavm.entity;

import tech.metavm.expression.EvaluationContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.IndexField;

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

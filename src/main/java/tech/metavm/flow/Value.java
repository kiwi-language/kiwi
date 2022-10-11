package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;

public abstract class Value {
    private final ValueType type;

    public Value(ValueDTO valueDTO) {
        type = ValueType.getByCodeRequired(valueDTO.type());
    }

    protected abstract Object getDTOValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        return new ValueDTO(type.code(), getDTOValue(persisting));
    }

    public abstract Object evaluate(EvaluationContext evaluationContext);

}

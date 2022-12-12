package tech.metavm.flow;

import tech.metavm.entity.ValueType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.meta.Type;

@ValueType("流程值")
public abstract class Value {
    private final ValueKind kind;

    public Value(ValueDTO valueDTO) {
        kind = ValueKind.getByCodeRequired(valueDTO.kind());
    }

    protected abstract Object getDTOValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        return new ValueDTO(kind.code(), getDTOValue(persisting), persisting ? null :
                getType().toDTO()
        );
    }

    public abstract Type getType();

    public abstract Object evaluate(EvaluationContext evaluationContext);

}

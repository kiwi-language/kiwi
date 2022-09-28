package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;

public abstract class Value {
    private final ValueType type;

    public Value(ValueDTO valueDTO) {
        type = ValueType.getByCodeRequired(valueDTO.type());
    }

    protected abstract Object getDTOValue();

    public ValueDTO toDTO() {
        return new ValueDTO(type.code(), getDTOValue());
    }

    public abstract Object evaluate(FlowFrame frame);

}

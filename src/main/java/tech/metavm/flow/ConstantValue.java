package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;

import java.util.Objects;

public class ConstantValue extends Value {

    private final Object value;

    public ConstantValue(ValueDTO valueDTO) {
        super(valueDTO);
        this.value = valueDTO.value();
    }

    @Override
    protected Object getDTOValue() {
        return value;
    }

    @Override
    public Object evaluate(FlowFrame frame) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantValue that = (ConstantValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ValueType.CONSTANT, value);
    }
}

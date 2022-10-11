package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;

import java.util.Objects;

public class ConstantValue extends Value {

    public static ConstantValue create(Object value) {
        return new ConstantValue(
                new ValueDTO(
                        ValueType.CONSTANT.code(),
                        value
                )
        );
    }

    private final Object value;

    public ConstantValue(ValueDTO valueDTO) {
        super(valueDTO);
        this.value = valueDTO.value();
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        return value;
    }

    @Override
    public Object evaluate(EvaluationContext context) {
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

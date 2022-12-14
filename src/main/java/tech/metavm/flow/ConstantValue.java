package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.ValueType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ValueUtil;

import java.util.Objects;

@ValueType("常量值")
public class ConstantValue extends Value {

    public static ConstantValue create(Object value/*, InstanceContext context*/) {
        return new ConstantValue(
                new ValueDTO(
                        ValueKind.CONSTANT.code(),
                        value,
                        null
                )/*,
                context*/
        );
    }

    @EntityField("值")
    private final Object value;

    public ConstantValue(ValueDTO valueDTO/*, InstanceContext context*/) {
        super(valueDTO/*, context*/);
        this.value = valueDTO.value();
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        return value;
    }

    @Override
    public Type getType() {
        return ValueUtil.getValueType(value);
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
        return Objects.hash(ValueKind.CONSTANT, value);
    }
}

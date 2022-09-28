package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.Objects;

public class FieldParam {
    private final Field field;
    private final Value value;

    public FieldParam(FieldParamDTO dto, EntityContext context) {
        this.field = context.getField(dto.fieldId());
        this.value = ValueFactory.getValue(dto.value());
    }

    public Field getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public FieldParamDTO toDTO() {
        return new FieldParamDTO(field.getId(), NncUtils.get(value, Value::toDTO));
    }

    public InstanceFieldDTO evaluate(FlowFrame executionContext) {
        return InstanceFieldDTO.valueOf(
                field.getId(),
                value.evaluate(executionContext)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldParam that = (FieldParam) o;
        return Objects.equals(field, that.field) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value);
    }
}

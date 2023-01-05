package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.Objects;

@EntityType("字段值")
public class FieldParam extends Entity {
    @EntityField("字段")
    private final Field field;
    @EntityField("值")
    private final Value value;

    public FieldParam(Field field, ValueDTO valueDTO, ParsingContext parsingContext) {
        this.field = field;
        this.value = ValueFactory.getValue(valueDTO, parsingContext);
    }

    public Field getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public FieldParamDTO toDTO(boolean persisting) {
        return new FieldParamDTO(field.getId(), NncUtils.get(value, v -> v.toDTO(persisting)));
    }

    public InstanceFieldDTO evaluate(FlowFrame executionContext) {
        return InstanceFieldDTO.valueOf(
                field.getId(),
                NncUtils.get(value, v -> getFieldValue(v, executionContext))
        );
    }

    private FieldValueDTO getFieldValue(Value value, EvaluationContext evaluationContext) {
        return value.evaluate(evaluationContext).toFieldValueDTO();
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

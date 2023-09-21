package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.Objects;

@EntityType("字段值")
public class FieldParam extends Entity {
    @EntityField("字段")
    private final Field field;
    @ChildEntity("值")
    private Value value;

    public FieldParam(Field field, ValueDTO valueDTO, ParsingContext parsingContext) {
        this(field, ValueFactory.create(valueDTO, parsingContext));
    }

    public FieldParam(Field field, Value value) {
        this.field = field;
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public FieldParamDTO toDTO(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new FieldParamDTO(
                    context.getRef(field), NncUtils.get(value, v -> v.toDTO(persisting)));
        }
    }

    public Instance evaluate(FlowFrame executionContext) {
        return value.evaluate(executionContext);
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

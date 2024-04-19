package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.NncUtils;

import java.util.Objects;

@EntityType("字段值")
public class FieldParam extends Entity implements LocalKey {

    public static FieldParam create(FieldParamDTO fieldParamDTO,
                                    ParsingContext parsingContext, IEntityContext entityContext) {
        return new FieldParam(
                entityContext.getField(Id.parse(fieldParamDTO.fieldId())),
                ValueFactory.create(fieldParamDTO.value(), parsingContext)
        );
    }

    @EntityField("字段")
    private final Field field;
    @ChildEntity("值")
    private Value value;

    public FieldParam(Field field, ValueDTO valueDTO, ParsingContext parsingContext) {
        this(field, ValueFactory.create(valueDTO, parsingContext));
    }

    public FieldParam(Field field, Value value) {
        this.field = field;
        setValue(value);
    }

    public Field getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = addChild(value, "value");
    }

    public FieldParamDTO toDTO() {
        try(var serContext = SerializeContext.enter()) {
            return new FieldParamDTO(
                    serContext.getId(this),
                    serContext.getId(field), NncUtils.get(value, Value::toDTO));
        }
    }

    public void update(FieldParamDTO fieldParamDTO, ParsingContext parsingContext) {
        if(fieldParamDTO.value() != null) {
            var value = ValueFactory.create(fieldParamDTO.value(), parsingContext);
            AssertUtils.assertTrue(field.getType().isAssignableFrom(value.getType(), null),
                    ErrorCode.INCORRECT_FIELD_VALUE, field.getName());
            setValue(value);
        }
    }

    public Instance evaluate(MetaFrame executionContext) {
        return value.evaluate(executionContext);
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

    @Override
    public boolean isValidLocalKey() {
        return field.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(field.getCode());
    }

    public String getText() {
        return field.getName() + ": " + value.getText();
    }

}

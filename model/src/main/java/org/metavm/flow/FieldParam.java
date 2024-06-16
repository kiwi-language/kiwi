package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.FieldParamDTO;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldRef;
import org.metavm.util.AssertUtils;
import org.metavm.util.NncUtils;

import java.util.Objects;

@EntityType
public class FieldParam extends Entity implements LocalKey {

    public static FieldParam create(FieldParamDTO fieldParamDTO,
                                    ParsingContext parsingContext, IEntityContext context) {
        return new FieldParam(
                FieldRef.create(fieldParamDTO.fieldRef(), context),
                ValueFactory.create(fieldParamDTO.value(), parsingContext)
        );
    }

    private final FieldRef fieldRef;
    private Value value;

    public FieldParam(FieldRef fieldRef, ValueDTO valueDTO, ParsingContext parsingContext) {
        this(fieldRef, ValueFactory.create(valueDTO, parsingContext));
    }

    public FieldParam(FieldRef fieldRef, Value value) {
        this.fieldRef = fieldRef;
        this.value = value;
    }

    public Field getField() {
        return fieldRef.resolve();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public FieldParamDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new FieldParamDTO(
                    serContext.getStringId(this),
                    fieldRef.toDTO(serContext), NncUtils.get(value, Value::toDTO));
        }
    }

    public void update(FieldParamDTO fieldParamDTO, ParsingContext parsingContext) {
        if (fieldParamDTO.value() != null) {
            var value = ValueFactory.create(fieldParamDTO.value(), parsingContext);
            AssertUtils.assertTrue(getField().getType().isAssignableFrom(value.getType()),
                    ErrorCode.INCORRECT_FIELD_VALUE, getField().getName());
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
        return Objects.equals(fieldRef, that.fieldRef) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldRef, value);
    }

    @Override
    public boolean isValidLocalKey() {
        return fieldRef.getRawField().getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(fieldRef.getRawField().getCode());
    }

    public String getText() {
        return fieldRef.getRawField().getName() + ": " + value.getText();
    }

}

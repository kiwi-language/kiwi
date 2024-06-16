package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.TryEndFieldDTO;
import org.metavm.object.type.Field;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class TryEndField extends Entity implements LocalKey {

    private final Field field;

    @ChildEntity
    private final ChildArray<TryEndValue> values = addChild(new ChildArray<>(TryEndValue.class), "values");

    private Value defaultValue;

    public TryEndField(Field field, List<TryEndValue> values, Value defaultValue, TryEndNode tryEndNode) {
        this.field = field;
        this.values.addChildren(values);
        this.defaultValue = defaultValue;
        tryEndNode.addField(this);
    }

    public List<TryEndValue> getValues() {
        return values.toList();
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Value defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Field getField() {
        return field;
    }

    public Value getValue(@Nullable NodeRT raiseNode) {
        if (raiseNode == null) {
            return defaultValue;
        } else {
            return NncUtils.requireNonNull(
                    values.get(TryEndValue::getRaiseNode, raiseNode),
                    "Can not find merge value of field " + field + " for raise node: " + raiseNode
            ).getValue();
        }
    }

    public TryEndFieldDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new TryEndFieldDTO(
                    field.getName(),
                    serContext.getStringId(field),
                    field.getType().toExpression(serContext),
                    NncUtils.map(values, TryEndValue::toDTO),
                    defaultValue.toDTO()
            );
        }
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
        return field.getName() + ": {" + NncUtils.join(values, TryEndValue::getText, ", ") + "}";
    }
}

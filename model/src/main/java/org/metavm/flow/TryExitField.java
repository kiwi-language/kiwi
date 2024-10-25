package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.TryExitFieldDTO;
import org.metavm.object.type.Field;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class TryExitField extends Entity implements LocalKey {

    private final Field field;

    @ChildEntity
    private final ChildArray<TryExitValue> values = addChild(new ChildArray<>(TryExitValue.class), "values");

    private Value defaultValue;

    public TryExitField(Field field, List<TryExitValue> values, Value defaultValue, TryExitNode tryExitNode) {
        this.field = field;
        this.values.addChildren(values);
        this.defaultValue = defaultValue;
        tryExitNode.addField(this);
    }

    public List<TryExitValue> getValues() {
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
                    values.get(TryExitValue::getRaiseNode, raiseNode),
                    "Can not find merge value of field " + field + " for raise node: " + raiseNode
            ).getValue();
        }
    }

    public TryExitFieldDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new TryExitFieldDTO(
                    field.getName(),
                    serContext.getStringId(field),
                    field.getType().toExpression(serContext),
                    NncUtils.map(values, TryExitValue::toDTO),
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
        return field.getName() + ": {" + NncUtils.join(values, TryExitValue::getText, ", ") + "}";
    }
}

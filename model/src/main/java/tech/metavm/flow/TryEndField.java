package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.TryEndFieldDTO;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class TryEndField extends Entity implements LocalKey {

    @EntityField("字段")
    private final Field field;

    @ChildEntity("值列表")
    private final ChildArray<TryEndValue> values = addChild(new ChildArray<>(TryEndValue.class), "values");

    @EntityField("默认值")
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

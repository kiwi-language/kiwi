package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.TryEndFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.entity.ChildArray;
import tech.metavm.util.NncUtils;
import tech.metavm.entity.ReadonlyArray;

import javax.annotation.Nullable;
import java.util.List;

public class TryEndField extends Entity {

    @EntityField("字段")
    private final Field field;

    @ChildEntity("值列表")
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

    public Value getValue(@Nullable NodeRT<?> raiseNode) {
        if(raiseNode == null) {
            return defaultValue;
        }
        else {
            return NncUtils.requireNonNull(
                    values.get(TryEndValue::getRaiseNode, raiseNode),
                    "Can not find merge value of field " + field + " for raise node: " + raiseNode
            ).getValue();
        }
    }

    public TryEndFieldDTO toDTO() {
        try(var context = SerializeContext.enter()) {
            return new TryEndFieldDTO(
                    field.getName(),
                    context.getRef(field),
                    context.getRef(field.getType()),
                    NncUtils.map(values, TryEndValue::toDTO),
                    defaultValue.toDTO(false)
            );
        }
    }
}

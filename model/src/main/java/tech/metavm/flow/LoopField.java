package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.LoopFieldDTO;
import tech.metavm.object.type.Field;

import java.util.Objects;

@EntityType("循环字段")
public class LoopField extends Entity implements LocalKey {

    @EntityField("字段")
    private final Field field;
    @ChildEntity("初始值")
    private Value initialValue;
    @ChildEntity("更新值")
    private Value updatedValue;

    public LoopField(Field field, Value initialValue, Value updatedValue) {
        this.field = field;
        this.initialValue = addChild(initialValue, "initialValue");
        this.updatedValue = addChild(updatedValue, "updatedValue");
    }

    public Field getField() {
        return field;
    }

    public Value getInitialValue() {
        return initialValue;
    }

    public Value getUpdatedValue() {
        return updatedValue;
    }

    public void setInitialValue(Value initialValue) {
        this.initialValue = addChild(initialValue, "initialValue");
    }

    public void setUpdatedValue(Value updatedValue) {
        this.updatedValue = addChild(updatedValue, "updatedValue");
    }

    public void update(LoopFieldDTO loopFieldDTO, ParsingContext parsingContext) {
        if (loopFieldDTO.initialValue() != null)
            setInitialValue(ValueFactory.create(loopFieldDTO.initialValue(), parsingContext));
        if (loopFieldDTO.updatedValue() != null)
            setUpdatedValue(ValueFactory.create(loopFieldDTO.updatedValue(), parsingContext));
    }

    public LoopFieldDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new LoopFieldDTO(
                    serContext.getStringId(field),
                    field.getName(),
                    field.getType().toExpression(serContext),
                    initialValue.toDTO(),
                    updatedValue.toDTO()
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
        return field.getName() + ": " + initialValue.getText() + "; " + updatedValue.getText();
    }
}

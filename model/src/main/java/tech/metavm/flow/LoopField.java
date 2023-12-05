package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.LoopFieldDTO;
import tech.metavm.object.type.Field;

@EntityType("循环字段")
public class LoopField extends Entity {

    @EntityField("字段")
    private Field field;
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

    public void update(LoopFieldDTO loopFieldDTO, IEntityContext context, ParsingContext parsingContext) {
        if(loopFieldDTO.fieldRef() != null) {
            field = context.getField(loopFieldDTO.fieldRef());
        }
        if(loopFieldDTO.initialValue() != null) {
            setInitialValue(ValueFactory.create(loopFieldDTO.initialValue(), parsingContext));
        }
        if(loopFieldDTO.updatedValue() != null) {
            setUpdatedValue(ValueFactory.create(loopFieldDTO.updatedValue(), parsingContext));
        }
    }

    public LoopFieldDTO toDTO(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new LoopFieldDTO(
                    context.getRef(field),
                    field.getName(),
                    context.getRef(field.getType()),
                    initialValue.toDTO(persisting),
                    updatedValue.toDTO(persisting)
            );
        }
    }
}

package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.Field;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static tech.metavm.object.type.Types.*;

@EntityType("更新字段")
public class UpdateField extends Entity {
    @EntityField("字段")
    private final Field field;
    @EntityField("操作")
    private UpdateOp op;
    @ChildEntity("值")
    private Value value;

    public UpdateField(Field field, UpdateOp op, Value value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

    public void execute(@Nullable ClassInstance instance, EvaluationContext context, boolean inConstructor, IInstanceContext instanceContext) {
        Instance evaluatedValue = value.evaluate(context);
        Instance updateValue;
        if(op == UpdateOp.SET) {
            updateValue = evaluatedValue;
        }
        else if(op == UpdateOp.INCREASE) {
            if(isDouble(field.getType())) {
                updateValue = field.getDouble(instance).add((DoubleInstance) evaluatedValue);
            }
            else if(isLong(field.getType())) {
                updateValue = field.getLong(instance).add((LongInstance) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else if(op == UpdateOp.DECREASE) {
            if(isDouble(field.getType())) {
                updateValue = field.getDouble(instance).minus((DoubleInstance) evaluatedValue);
            }
            else if(isLong(field.getType())) {
                updateValue = field.getLong(instance).minus((LongInstance) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else {
            throw new InternalException("Unsupported update operation: " + op);
        }
        if(field.isStatic()) {
            field.setStaticValue(updateValue);
        }
        else {
            NncUtils.requireNonNull(instance);
            if (inConstructor && !instance.isFieldInitialized(field)) {
                instance.initField(field, updateValue);
            } else {
                instance.setField(field, updateValue);
            }
        }
    }

    public Field getField() {
        return field;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void setOp(UpdateOp op) {
        this.op = op;
    }

    public UpdateFieldDTO toDTO(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new UpdateFieldDTO(
                    context.getRef(field),
                    op.code(),
                    value.toDTO(persisting)
            );
        }
    }

    public UpdateOp getOp() {
        return op;
    }

    public Value getValue() {
        return value;
    }
}

package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.InstanceContext;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InternalException;

import static tech.metavm.object.meta.TypeUtil.*;

@EntityType("更新字段")
public class UpdateField extends Entity {
    @EntityField("字段")
    private final Field field;
    @EntityField("操作")
    private final UpdateOp op;
    @EntityField("值")
    private final Value value;

    public UpdateField(ClassType declaringType, UpdateFieldDTO updateFieldDTO, ParsingContext parsingContext) {
        this(
                declaringType.getField(updateFieldDTO.fieldId()),
                UpdateOp.getByCode(updateFieldDTO.opCode()),
                ValueFactory.getValue(updateFieldDTO.value(), parsingContext)
        );
    }

    public UpdateField(Field field, UpdateOp op, Value value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

    public void execute(ClassInstance instance, EvaluationContext context, InstanceContext instanceContext) {
        Instance evaluatedValue = value.evaluate(context);
        Instance updateValue;
        if(op == UpdateOp.SET) {
            updateValue = evaluatedValue;
        }
        else if(op == UpdateOp.INCREASE) {
            if(isDouble(field.getType())) {
                updateValue = instance.getDouble(field.getId()).add((DoubleInstance) evaluatedValue);
            }
            else if(isInt(field.getType())) {
                updateValue = instance.getInt(field.getId()).add((IntInstance) evaluatedValue);
            }
            else if(isLong(field.getType())) {
                updateValue = instance.getLong(field.getId()).add((LongInstance) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else if(op == UpdateOp.DECREASE) {
            if(isDouble(field.getType())) {
                updateValue = instance.getDouble(field.getId()).subtract((DoubleInstance) evaluatedValue);
            }
            else if(isInt(field.getType())) {
                updateValue = instance.getInt(field.getId()).subtract((IntInstance) evaluatedValue);
            }
            else if(isLong(field.getType())) {
                updateValue = instance.getLong(field.getId()).subtract((LongInstance) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else {
            throw new InternalException("Unsupported update operation: " + op);
        }

        instance.set(field, updateValue);
    }

    public UpdateFieldDTO toDTO(boolean persisting) {
        return new UpdateFieldDTO(
                field.getId(),
                op.code(),
                value.toDTO(persisting)
        );
    }
}

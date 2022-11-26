package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.ValueType;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;

@ValueType("更新字段")
public class UpdateField {
    @EntityField("字段")
    private final Field field;
    @EntityField("操作")
    private final UpdateOp op;
    @EntityField("值")
    private final Value value;

    public UpdateField(Type declaringType, UpdateFieldDTO updateFieldDTO, ParsingContext parsingContext) {
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

    public void execute(Instance instance, EvaluationContext context) {
        Object evaluatedValue = value.evaluate(context);
        Object updateValue;
        if(op == UpdateOp.SET) {
            updateValue = evaluatedValue;
        }
        else if(op == UpdateOp.INCREASE) {
            if(field.getType().isDouble()) {
                updateValue = instance.getDouble(field.getId()) + (double) evaluatedValue;
            }
            else if(field.getType().isInt()) {
                updateValue = instance.getInt(field.getId()) + (int) evaluatedValue;
            }
            else if(field.getType().isLong()) {
                updateValue = instance.getLong(field.getId()) + (long) evaluatedValue;
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else if(op == UpdateOp.DECREASE) {
            if(field.getType().isDouble()) {
                updateValue = instance.getDouble(field.getId()) - (double) evaluatedValue;
            }
            else if(field.getType().isInt()) {
                updateValue = instance.getInt(field.getId()) - (int) evaluatedValue;
            }
            else if(field.getType().isLong()) {
                updateValue = instance.getLong(field.getId()) - (long) evaluatedValue;
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else {
            throw new InternalException("Unsupported update operation: " + op);
        }
        instance.setRawFieldValue(InstanceFieldDTO.valueOf(field.getId(), updateValue));
    }

    public UpdateFieldDTO toDTO(boolean persisting) {
        return new UpdateFieldDTO(
                field.getId(),
                op.code(),
                value.toDTO(persisting)
        );
    }
}

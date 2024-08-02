package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.LocalKey;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.EvaluationContext;
import org.metavm.flow.rest.UpdateFieldDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DoubleValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Types;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class UpdateField extends Entity implements LocalKey {
    private final FieldRef fieldRef;
    private UpdateOp op;
    private org.metavm.flow.Value value;

    public UpdateField(FieldRef fieldRef, UpdateOp op, org.metavm.flow.Value value) {
        this.fieldRef = fieldRef;
        this.op = op;
        this.value = value;
    }

    public void execute(@Nullable ClassInstance instance, EvaluationContext context, boolean inConstructor) {
        Value evaluatedValue = value.evaluate(context);
        Value updateValue;
        var field = fieldRef.resolve();
        if(op == UpdateOp.SET)
            updateValue = evaluatedValue;
        else if(op == UpdateOp.INC) {
            if(Types.isDouble(field.getType())) {
                updateValue = field.getDouble(instance).add((DoubleValue) evaluatedValue);
            }
            else if(Types.isLong(field.getType())) {
                updateValue = field.getLong(instance).add((LongValue) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType());
            }
        }
        else if(op == UpdateOp.DEC) {
            if(Types.isDouble(field.getType())) {
                updateValue = field.getDouble(instance).minus((DoubleValue) evaluatedValue);
            }
            else if(Types.isLong(field.getType())) {
                updateValue = field.getLong(instance).minus((LongValue) evaluatedValue);
            }
            else {
                throw new InternalException("Update operation: " + op + " is not supported for field type: " + field.getType().toExpression());
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

    public FieldRef getFieldRef() {
        return fieldRef;
    }

    public void setValue(org.metavm.flow.Value value) {
        this.value = value;
    }

    public void setOp(UpdateOp op) {
        this.op = op;
    }

    public UpdateFieldDTO toDTO() {
        try(var serContext = SerializeContext.enter()) {
            return new UpdateFieldDTO(
                    fieldRef.toDTO(serContext),
                    fieldRef.getRawField().getName(),
                    op.code(),
                    value.toDTO()
            );
        }
    }

    public UpdateOp getOp() {
        return op;
    }

    public org.metavm.flow.Value getValue() {
        return value;
    }

    public String getText() {
        return fieldRef.getRawField().getName() + " " + op.op() + " " + value.getText();
    }

    @Override
    public boolean isValidLocalKey() {
        return fieldRef.getRawField().getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(fieldRef.getRawField().getCode());
    }

}

package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;

import java.util.function.Consumer;

@Wire(12)
@Entity
public class AddFieldTask extends ScanByClassTask {

    private final Reference fieldReference;

    protected AddFieldTask(Id id, ClassType type, Field field) {
        super(id, "Add field", type);
        this.fieldReference = field.getReference();
    }

    @Override
    protected void processClassInstance(ClassInstance instance, IInstanceContext context) {
        Value fieldValue;
        var field = getField();
        if(field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType).getReference();
        else
            fieldValue = Instances.computeFieldInitialValue(instance, field, context);
        if(!instance.isFieldInitialized(field))
            instance.initField(field, fieldValue);
    }

    public Field getField() {
        return (Field) fieldReference.get();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(fieldReference);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}

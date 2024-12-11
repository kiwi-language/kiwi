package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

@Entity
public class AddFieldTask extends ScanByClassTask {

    private final Field field;

    protected AddFieldTask(Klass declaringType, Field field) {
        super(String.format("Add field task %s.%s", declaringType.getName(), field.getName()), declaringType.getType());
        this.field = field;
    }

    @Override
    protected void processClassInstance(ClassInstance instance, IEntityContext context) {
        Value fieldValue;
        if(field.isChild() && field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType).getReference();
        else
            fieldValue = Instances.computeFieldInitialValue(instance, field, context);
        if(!instance.isFieldInitialized(field))
            instance.initField(field, fieldValue);
    }

    public Field getField() {
        return field;
    }
}

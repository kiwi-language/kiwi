package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;

@EntityType
public class AddFieldTask extends ScanByClassTask {

    private final Field field;

    protected AddFieldTask(Klass declaringType, Field field) {
        super(String.format("Add field task %s.%s", declaringType.getName(), field.getName()), declaringType.getType());
        this.field = field;
    }

    @Override
    protected void processClassInstance(ClassInstance instance) {
        Instance fieldValue;
        if(field.isChild() && field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType);
        else
            fieldValue = field.getDefaultValue();
        if(!instance.isFieldInitialized(field))
            instance.initField(field, fieldValue);
    }

}

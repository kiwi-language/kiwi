package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

@EntityType("添加字段任务")
public class AddFieldTask extends ScanByClassTask {

    @EntityField("字段")
    private final Field field;

    protected AddFieldTask(ClassType declaringType, Field field) {
        super(String.format("%s新增字段%s任务", declaringType.getName(), field.getName()), declaringType);
        this.field = field;
    }

    @Override
    protected void processClassInstance(ClassInstance instance) {
        Instance fieldValue;
        if(field.isChild() && field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType);
        else
            fieldValue = field.getDefaultValue();
        instance.initField(field, fieldValue);
    }

}

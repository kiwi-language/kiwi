package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;

@EntityType("添加字段任务")
public class AddFieldTask extends ScanByClassTask {

    @EntityField("字段数据")
    private final FieldData fieldData;

    protected AddFieldTask(ClassType declaringType, FieldData data) {
        super(declaringType.getName() + "新增字段" + data.getName() + " 任务", declaringType);
        this.fieldData = data;
    }

    @Override
    protected void processClassInstance(ClassInstance instance) {
        Instance fieldValue;
        if(fieldData.isChild() && fieldData.getType() instanceof ArrayType arrayType) {
            fieldValue = new ArrayInstance(arrayType);
        }
        else {
            fieldValue = fieldData.getDefaultValue();
        }
        instance.setDirtyField(fieldData.getDeclaringType(), fieldData.getColumn(), fieldValue);
    }

}

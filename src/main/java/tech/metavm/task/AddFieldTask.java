package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.ClassType;

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
        instance.setDirtyField(fieldData.getDeclaringType(), fieldData.getColumn(), fieldData.getDefaultValue());
    }

}

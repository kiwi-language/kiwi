package tech.metavm.task;

import tech.metavm.entity.*;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.MetadataState;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType("添加字段任务组")
public class AddFieldJobGroup extends TaskGroup {

    @EntityField("字段")
    private final Field field;
    @ChildEntity("模版实例字段列表")
    private final ReadWriteArray<Field> templateInstanceFields =
            addChild(new ReadWriteArray<>(Field.class), "templateInstanceFields");

    public AddFieldJobGroup(Field field) {
        this.field = field;
    }

    public List<Task> createTasks(IEntityContext context) {
        var type = field.getDeclaringType();
        List<Task> jobs = new ArrayList<>();
        if (type.isTemplate()) {
            var templateInstances = context.selectByKey(Klass.TEMPLATE_IDX, type);
            for (Klass templateInstance : templateInstances) {
                context.getGenericContext().retransformClass(type, templateInstance);
                var tiField = NncUtils.findRequired(templateInstance.getFields(), f -> f.getCopySource() == field);
                templateInstanceFields.add(tiField);
                createJobsForType(templateInstance, tiField, jobs);
            }
        } else {
            createJobsForType(type, field, jobs);
        }
        return jobs;
    }

    private void createJobsForType(Klass type, Field field, List<Task> jobs) {
        jobs.add(new AddFieldTask(type, field));
        for (Klass subType : type.getSubTypes()) {
            createJobsForType(subType, field, jobs);
        }
    }

    @Override
    public void onTasksDone(IEntityContext context) {
        List<Field> fields = NncUtils.prepend(field, templateInstanceFields.toList());
        for (Field field : fields)
            field.setState(MetadataState.READY);
    }

}

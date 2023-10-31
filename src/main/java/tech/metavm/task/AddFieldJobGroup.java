package tech.metavm.task;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.TaskGroup;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.FieldBuilder;
import tech.metavm.util.ChildArray;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType("添加字段任务组")
public class AddFieldJobGroup extends TaskGroup {

    @ChildEntity("字段")
    private final FieldData data;
    @ChildEntity("模版实例字段列表")
    private final ChildArray<FieldData> transformedFieldData = new ChildArray<>(FieldData.class);

    public AddFieldJobGroup(FieldData fieldData) {
        this.data = fieldData;
    }

    public List<Task> createJobs(IEntityContext context) {
        var type = data.getDeclaringType();
        List<Task> jobs = new ArrayList<>();
        if(type.isTemplate()) {
            var templateInstances = context.selectByKey(ClassType.TEMPLATE_IDX, type);
            for (ClassType templateInstance : templateInstances) {
                var transformedData = context.getGenericContext().transformFieldData(
                        type, templateInstance, data
                );
                transformedFieldData.addChild(transformedData);
                createJobsForType(templateInstance, transformedData, jobs);
            }
        }
        else {
            createJobsForType(type, data, jobs);
        }
        return jobs;
    }

    private void createJobsForType(ClassType type, FieldData data, List<Task> jobs) {
        jobs.add(new AddFieldTask(type, data));
        for (ClassType subType : type.getSubTypes()) {
            createJobsForType(subType, data, jobs);
        }
    }

    @Override
    public void onJobsDone(IEntityContext context) {
        List<FieldData> allFieldData = NncUtils.prepend(data, transformedFieldData.toList());
        for (FieldData data : allFieldData) {
            var field = FieldBuilder.newBuilder(data.getName(), data.getCode(), data.getDeclaringType(), data.getType())
                    .isChild(data.isChild())
                    .asTitle(data.isAsTitle())
                    .defaultValue(data.getDefaultValue())
                    .access(data.getAccess())
                    .column(data.getColumn())
                    .unique(data.isUnique())
                    .build();
            context.bind(field);
        }
    }
}

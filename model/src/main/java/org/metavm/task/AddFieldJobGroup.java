package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType
public class AddFieldJobGroup extends TaskGroup {

    private final Field field;
    @ChildEntity
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

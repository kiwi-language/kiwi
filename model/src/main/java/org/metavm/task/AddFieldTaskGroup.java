package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AddFieldTaskGroup extends TaskGroup {

    private final Field field;

    public AddFieldTaskGroup(Field field) {
        this.field = field;
    }

    public List<Task> createTasks(IEntityContext context) {
        var klass = field.getDeclaringType();
        List<Task> tasks = new ArrayList<>();
        createTasksForKlass(klass, field, tasks);
        return tasks;
    }

    private void createTasksForKlass(Klass klass, Field field, List<Task> tasks) {
        tasks.add(new AddFieldTask(klass, field));
        for (Klass subKlass : klass.getSubKlasses()) {
            createTasksForKlass(subKlass, field, tasks);
        }
    }

    @Override
    public void onCompletion(IEntityContext context, IEntityContext taskContext) {
        field.setState(MetadataState.READY);
    }

}

package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire(71)
@Entity
public class AddFieldTaskGroup extends TaskGroup {

    private final Reference fieldReference;

    public AddFieldTaskGroup(Id id, Field field) {
        super(id);
        this.fieldReference = field.getReference();
    }

    public List<Task> createTasks(IInstanceContext context) {
        var field = getField();
        var klass = field.getDeclaringType();
        List<Task> tasks = new ArrayList<>();
        createTasksForKlass(klass, field, tasks);
        return tasks;
    }

    private void createTasksForKlass(Klass klass, Field field, List<Task> tasks) {
        tasks.add(new AddFieldTask(nextChildId(), klass.getType(), field));
        for (Klass subKlass : klass.getSubKlasses()) {
            createTasksForKlass(subKlass, field, tasks);
        }
    }

    @Override
    public void onCompletion(IInstanceContext context, IInstanceContext taskContext) {
        getField().setState(MetadataState.READY);
    }

    public Field getField() {
        return (Field) fieldReference.get();
    }

    @Override
    public String getTitle() {
        return null;
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

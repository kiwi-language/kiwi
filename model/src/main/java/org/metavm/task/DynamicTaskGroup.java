package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
@Wire(10)
public abstract class DynamicTaskGroup extends TaskGroup {

    private int activeTaskCount;

    public DynamicTaskGroup(Id id) {
        super(id);
    }

    @Override
    public void onBind(IInstanceContext context) {
        super.onBind(context);
        activeTaskCount = getTasks().size();
    }

    @Override
    public void onTaskCompletion(Task task, IInstanceContext context, IInstanceContext taskContext) {
        activeTaskCount--;
        if(getTasks().contains(task))
            super.onTaskCompletion(task, context, taskContext);
        else {
            if(isCompleted())
                onCompletion(context, taskContext);
        }
    }

    @Override
    public boolean isCompleted() {
        return activeTaskCount <= 0;
    }

    public void addTask(Task task) {
        task.setGroup(this);
        activeTaskCount++;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}

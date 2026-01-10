package org.manul.task;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

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

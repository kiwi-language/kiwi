package org.metavm.task;

import org.metavm.entity.IEntityContext;

public abstract class DynamicTaskGroup extends TaskGroup {

    private int activeTaskCount;

    @Override
    public void onBind(IEntityContext context) {
        super.onBind(context);
        activeTaskCount = getTasks().size();
    }

    @Override
    public void onDone(Task task, IEntityContext context, IEntityContext taskContext) {
        activeTaskCount--;
        if(getTasks().contains(task))
            super.onDone(task, context, taskContext);
        else {
            taskContext.remove(task);
            if(isDone())
                onTasksDone(context, taskContext);
        }
    }

    @Override
    public boolean isDone() {
        return activeTaskCount <= 0;
    }

    public void addTask(Task task) {
        task.setGroup(this);
        activeTaskCount++;
    }
}

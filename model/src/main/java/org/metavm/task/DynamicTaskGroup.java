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
    public void onTaskCompletion(Task task, IEntityContext context, IEntityContext taskContext) {
        activeTaskCount--;
        if(getTasks().contains(task))
            super.onTaskCompletion(task, context, taskContext);
        else {
            taskContext.remove(task);
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
}

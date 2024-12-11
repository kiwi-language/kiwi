package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.ChildArray;
import org.metavm.entity.IEntityContext;
import org.metavm.util.Constants;

import java.util.List;

@Entity
public abstract class TaskGroup extends org.metavm.entity.Entity {

    private long completedTaskCount;
    private boolean failed;

    @ChildEntity
    private final ChildArray<Task> tasks = addChild(new ChildArray<>(Task.class), "tasks");

    public TaskGroup() {
        System.out.println(tasks);
    }

    public void onTaskCompletion(Task task, IEntityContext context, IEntityContext taskContext) {
        completedTaskCount++;
        if(isCompleted()) {
            onCompletion(context, taskContext);
        }
    }

    public void onTaskFailure(Task task, IEntityContext context, IEntityContext taskContext) {
        failed = true;
    }

    @Override
    public void onBind(IEntityContext context) {
        tasks.addChildren(createTasks(context));
        for (Task job : tasks) {
            job.setGroup(this);
        }
        if(tasks.isEmpty()) {
            onCompletion(context, context);
        }
    }

    public boolean isCompleted() {
        return completedTaskCount == tasks.size();
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isTerminated() {
        return isCompleted() || isFailed();
    }

    public abstract List<Task> createTasks(IEntityContext context);

    protected abstract void onCompletion(IEntityContext context, IEntityContext taskContext);

    public List<Task> getTasks() {
        return tasks.toList();
    }

    public long getSessionTimeout() {
        return Constants.SESSION_TIMEOUT;
    }

}

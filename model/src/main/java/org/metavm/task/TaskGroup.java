package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ChildArray;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;

import java.util.List;

@EntityType
public abstract class TaskGroup extends Entity {

    private long completedTaskCount;

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

    public abstract List<Task> createTasks(IEntityContext context);

    protected abstract void onCompletion(IEntityContext context, IEntityContext taskContext);

    public List<Task> getTasks() {
        return tasks.toList();
    }
}

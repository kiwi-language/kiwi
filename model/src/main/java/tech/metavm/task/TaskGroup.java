package tech.metavm.task;

import tech.metavm.entity.*;

import java.util.List;

@EntityType
public abstract class TaskGroup extends Entity {

    private long numDone;

    @ChildEntity
    private final ChildArray<Task> tasks = addChild(new ChildArray<>(Task.class), "tasks");

    public TaskGroup() {
        System.out.println(tasks);
    }

    public void onDone(Task task, IEntityContext context) {
        numDone++;
        if(numDone == tasks.size()) {
            onTasksDone(context);
        }
    }

    @Override
    public void onBind(IEntityContext context) {
        tasks.addChildren(createTasks(context));
        for (Task job : tasks) {
            job.setGroup(this);
        }
        if(tasks.isEmpty()) {
            onTasksDone(context);
        }
    }

    public boolean isDone() {
        return numDone == tasks.size();
    }

    public abstract List<Task> createTasks(IEntityContext context);

    protected abstract void onTasksDone(IEntityContext context);

}

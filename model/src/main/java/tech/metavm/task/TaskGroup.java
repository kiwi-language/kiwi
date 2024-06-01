package tech.metavm.task;

import tech.metavm.entity.*;

import java.util.List;

@EntityType("任务组")
public abstract class TaskGroup extends Entity {

    @EntityField("已完成任务数")
    private long numDone;

    @ChildEntity("任务列表")
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

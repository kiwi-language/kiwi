package tech.metavm.object.instance;

import tech.metavm.entity.*;
import tech.metavm.task.Task;
import tech.metavm.entity.ChildArray;

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

    public void onDone(Task job, IEntityContext context) {
        numDone++;
        if(numDone == tasks.size()) {
            onJobsDone(context);
        }
    }

    @Override
    public void onBind(IEntityContext context) {
        tasks.addChildren(createJobs(context));
        for (Task job : tasks) {
            job.setGroup(this);
        }
        if(tasks.isEmpty()) {
            onJobsDone(context);
        }
    }

    public boolean isDone() {
        return numDone == tasks.size();
    }

    public abstract List<Task> createJobs(IEntityContext context);

    protected abstract void onJobsDone(IEntityContext context);

}

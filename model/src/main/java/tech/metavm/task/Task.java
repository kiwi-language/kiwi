package tech.metavm.task;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.instance.core.IInstanceContext;

import javax.annotation.Nullable;

@EntityType("任务")
public abstract class Task extends Entity {

    public static final IndexDef<Task> IDX_STATE_LAST_RUN_AT = new IndexDef<>(
            Task.class,false, "state", "lastRunAt"
    );

    protected static final int BATCH_SIZE = 1000;

    @EntityField("标题")
    private final String title;
    @EntityField("状态")
    private TaskState state = TaskState.RUNNABLE;
    @EntityField("最近执行时间")
    private long lastRunAt;
    @EntityField("执行次数")
    private long numRuns;
    @EntityField("分组")
    private @Nullable TaskGroup group;

    protected Task(String title) {
        this.title = title;
    }

    protected abstract boolean run0(IInstanceContext context);

    public void run(IInstanceContext context) {
        numRuns++;
        if(run0(context)) {
            if(group != null) {
                group.onDone(this, context.getEntityContext());
            }
            state = TaskState.FINISHED;
        }
        else {state = TaskState.RUNNABLE;}
    }


    @Nullable
    public TaskGroup getGroup() {
        return group;
    }

    public void setGroup(@Nullable TaskGroup group) {
        this.group = group;
    }

    public String getTitle() {
        return title;
    }

    public long getNumRuns() {
        return numRuns;
    }

    public void setLastRunAt(long lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public TaskState getState() {
        return state;
    }

    public boolean isRunnable() {
        return state == TaskState.RUNNABLE;
    }

    public boolean isRunning() {
        return state == TaskState.RUNNING;
    }

    public boolean isFinished() {
        return state == TaskState.FINISHED;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getLastRunAt() {
        return lastRunAt;
    }

}

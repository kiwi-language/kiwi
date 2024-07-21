package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;

import javax.annotation.Nullable;

@EntityType
public abstract class Task extends Entity {

    public static final IndexDef<Task> IDX_STATE_LAST_RUN_AT = new IndexDef<>(
            Task.class,false, "state", "lastRunTimestamp"
    );

    protected static final int BATCH_SIZE = 1000;

    private final String title;
    private TaskState state = TaskState.RUNNABLE;
    private long lastRunTimestamp;
    private long runCount;
    @Nullable
    private TaskGroup group;

    protected Task(String title) {
        this.title = title;
    }

    protected abstract boolean run0(IEntityContext context, IEntityContext taskContext);

    public void run(IEntityContext executionContext, IEntityContext taskContext) {
        runCount++;
        if(run0(executionContext, taskContext)) {
            if(group != null) {
                group.onTaskCompletion(this, executionContext, taskContext);
            }
            state = TaskState.COMPLETED;
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

    public long getRunCount() {
        return runCount;
    }

    public void setLastRunTimestamp(long lastRunTimestamp) {
        this.lastRunTimestamp = lastRunTimestamp;
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

    public boolean isCompleted() {
        return state == TaskState.COMPLETED;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getLastRunTimestamp() {
        return lastRunTimestamp;
    }

}

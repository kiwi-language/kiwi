package org.metavm.task;

import org.metavm.entity.*;

import javax.annotation.Nullable;

@EntityType
public abstract class Task extends Entity {

    public static final IndexDef<Task> IDX_STATE_LAST_RUN_AT = new IndexDef<>(
            Task.class,false, "state", "lastRunAt"
    );

    protected static final int BATCH_SIZE = 1000;

    private final String title;
    private TaskState state = TaskState.RUNNABLE;
    private long lastRunAt;
    private long numRuns;
    @Nullable
    private TaskGroup group;

    protected Task(String title) {
        this.title = title;
    }

    protected abstract boolean run0(IEntityContext context);

    public void run(IEntityContext context) {
        numRuns++;
        if(run0(context)) {
            if(group != null) {
                group.onDone(this, context);
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

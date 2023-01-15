package tech.metavm.job;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.IndexDef;

@EntityType("任务")
public abstract class Job extends Entity {

    public static final IndexDef<Job> IDX_STATE_LASTED_SCHEDULED_AT = new IndexDef<>(
            Job.class, "state", "lastRunAt"
    );

    private final String title;
    private JobState state = JobState.RUNNABLE;
    private long lastRunAt;
    private long numRuns;

    protected Job(String title) {
        this.title = title;
    }

    protected abstract boolean run0(IInstanceContext context);

    public void run(IInstanceContext context) {
        numRuns++;
        if(run0(context)) {
            state = JobState.FINISHED;
        }
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

    public JobState getState() {
        return state;
    }

    public boolean isRunnable() {
        return state == JobState.RUNNABLE;
    }

    public boolean isFinished() {
        return state == JobState.FINISHED;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public long getLastRunAt() {
        return lastRunAt;
    }

}

package tech.metavm.job;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

@EntityType("任务信号")
public class JobSignal extends Entity {

    public static final IndexDef<JobSignal> IDX_TENANT_ID = new IndexDef<>(
            JobSignal.class, true, "tenantId"
    );

    public static final IndexDef<JobSignal> IDX_LAST_JOB_CREATED_AT = new IndexDef<>(
            JobSignal.class, "lastJobCreatedAt"
    );

    private final long tenantId;
    private long unfinishedCount;
    private long lastJobCreatedAt;
    private long lastRunJobId;

    public JobSignal(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public long getUnfinishedCount() {
        return unfinishedCount;
    }

    public void setUnfinishedCount(long unfinishedCount) {
        this.unfinishedCount = unfinishedCount;
    }

    public long getLastJobCreatedAt() {
        return lastJobCreatedAt;
    }

    public void setLastJobCreatedAt(long lastJobCreatedAt) {
        this.lastJobCreatedAt = lastJobCreatedAt;
    }

    public long getLastRunJobId() {
        return lastRunJobId;
    }

    public void setLastRunJobId(long lastRunJobId) {
        this.lastRunJobId = lastRunJobId;
    }

    public boolean decreaseUnfinishedJobCount() {
        unfinishedCount--;
        return unfinishedCount <= 0;
    }

    public boolean hasUnfinishedJobs() {
        return unfinishedCount > 0;
    }

}

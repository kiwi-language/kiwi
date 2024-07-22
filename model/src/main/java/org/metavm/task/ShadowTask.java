package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;

import javax.annotation.Nullable;

@EntityType
public class ShadowTask extends Entity {

    public static final IndexDef<ShadowTask> IDX_RUN_AT = IndexDef.create(ShadowTask.class,"runAt");
    public static final IndexDef<ShadowTask> IDX_EXECUTOR_IP_START_AT = IndexDef.create(ShadowTask.class, "executorIP", "startAt");

    private TaskState state = TaskState.RUNNABLE;
    private @Nullable String executorIP;
    private final long startAt;
    private long runAt;
    private final long appId;
    private final String appTaskId;

    public ShadowTask(long appId, String appTaskId, long startAt) {
        this.appId = appId;
        this.appTaskId = appTaskId;
        this.startAt = startAt;
    }

    public long getAppId() {
        return appId;
    }

    public String getAppTaskId() {
        return appTaskId;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getRunAt() {
        return runAt;
    }

    public void setRunAt(long runAt) {
        this.runAt = runAt;
    }

    @Nullable
    public String getExecutorIP() {
        return executorIP;
    }

    public void setExecutorIP(@Nullable String executorIP) {
        this.executorIP = executorIP;
    }
}
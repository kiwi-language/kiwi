package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.WAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

@Entity
public class ShadowTask extends org.metavm.entity.Entity {

    public static final Logger logger = LoggerFactory.getLogger(ShadowTask.class);

    public static final IndexDef<ShadowTask> IDX_RUN_AT = IndexDef.create(ShadowTask.class,"runAt");
    public static final IndexDef<ShadowTask> IDX_EXECUTOR_IP_START_AT = IndexDef.create(ShadowTask.class, "executorIP", "startAt");

    private TaskState state = TaskState.RUNNABLE;
    private @Nullable String executorIP;
    private final long startAt;
    private long runAt;
    private final long appId;
    private final String appTaskId;
    private final @Nullable WAL defWal;

    public static BiConsumer<Long, List<Task>> saveShadowTasksHook;

    public ShadowTask(long appId, String appTaskId, long startAt, @Nullable WAL defWal) {
        this.appId = appId;
        this.appTaskId = appTaskId;
        this.startAt = startAt;
        this.defWal = defWal;
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

    @Nullable
    public WAL getDefWal() {
        return defWal;
    }
}
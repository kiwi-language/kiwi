package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;

@EntityType
public class TaskSignal extends Entity {

    public static final IndexDef<TaskSignal> IDX_APP_ID = new IndexDef<>(
            TaskSignal.class, true, "appId"
    );

    public static final IndexDef<TaskSignal> IDX_LAST_TASK_CREATED_AT = new IndexDef<>(
            TaskSignal.class, false,"lastTaskCreatedAt"
    );

    private final long appId;
    private long unfinishedCount;
    private long lastTaskCreatedAt;
    private long lastRunTaskId;

    public TaskSignal(long appId) {
        this.appId = appId;
    }

    public long getAppId() {
        return appId;
    }

    public long getUnfinishedCount() {
        return unfinishedCount;
    }

    public void setUnfinishedCount(long unfinishedCount) {
        this.unfinishedCount = unfinishedCount;
    }

    public long getLastTaskCreatedAt() {
        return lastTaskCreatedAt;
    }

    public void setLastTaskCreatedAt(long lastTaskCreatedAt) {
        this.lastTaskCreatedAt = lastTaskCreatedAt;
    }

    public long getLastRunTaskId() {
        return lastRunTaskId;
    }

    public void setLastRunTaskId(long lastRunTaskId) {
        this.lastRunTaskId = lastRunTaskId;
    }

    public boolean decreaseUnfinishedTaskCount() {
        unfinishedCount--;
        return unfinishedCount <= 0;
    }

    public boolean hasUnfinishedTasks() {
        return unfinishedCount > 0;
    }

}

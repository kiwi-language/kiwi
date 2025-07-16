package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(33)
@Entity
@Slf4j
public abstract class Task extends org.metavm.entity.Entity {

    public static final IndexDef<Task> IDX_STATE_LAST_RUN_AT = IndexDef.create(Task.class,
            2, task -> List.of(
                    Instances.intInstance(task.state.code()),
                    Instances.longInstance(task.lastRunTimestamp)));

    protected static final int BATCH_SIZE = 1000;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private String title;
    private TaskState state = TaskState.RUNNABLE;
    private long lastRunTimestamp;
    private long runCount;
    private long startAt;
    @Nullable
    private TaskGroup group;

    protected Task(@NotNull Id id, String title) {
        super(id);
        this.title = title;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitByte();
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitLong();
    }

    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        var r = run1(context, taskContext);
        context.validate();
        return r;
    }

    protected abstract boolean run1(IInstanceContext context, IInstanceContext taskContext);

    public void run(IInstanceContext executionContext, IInstanceContext taskContext) {
        try {
            runCount++;
            if (run0(executionContext, taskContext)) {
                if (group != null) {
                    group.onTaskCompletion(this, executionContext, taskContext);
                }
                onSuccess(executionContext, taskContext);
                state = TaskState.COMPLETED;
            } else {
                state = TaskState.RUNNABLE;
            }
        }
        catch (Exception e) {
            try {
                onFailure(taskContext);
            }
            catch (Exception e1) {
                log.error("Failed to execute onFailure for task {}-{}", getTitle(), getStringId(), e1);
            }
            state = TaskState.FAILED;
            if(group != null)
                group.onTaskFailure(this, executionContext, taskContext);
            throw e;
        }
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

    public boolean isFailed() {
        return state == TaskState.FAILED;
    }

    public boolean isTerminated() {
        return isCompleted() || isFailed();
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getLastRunTimestamp() {
        return lastRunTimestamp;
    }

    protected void onFailure(IInstanceContext taskContext) {
    }

    protected void onSuccess(IInstanceContext context, IInstanceContext taskContext) {}

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getTimeout() {
        return group != null ? group.getSessionTimeout() : Constants.SESSION_TIMEOUT;
    }

    public List<Id> getExtraStdKlassIds() {
        return List.of();
    }

    public boolean isRelocationEnabled() {
        return false;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return group;
    }

    public boolean isMigrating() {
        return false;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        var group = this.getGroup();
        if (group != null) map.put("group", group.getStringId());
        map.put("runCount", this.getRunCount());
        map.put("state", this.getState().name());
        map.put("runnable", this.isRunnable());
        map.put("running", this.isRunning());
        map.put("completed", this.isCompleted());
        map.put("failed", this.isFailed());
        map.put("terminated", this.isTerminated());
        map.put("lastRunTimestamp", this.getLastRunTimestamp());
        map.put("startAt", this.getStartAt());
        map.put("timeout", this.getTimeout());
        map.put("extraStdKlassIds", this.getExtraStdKlassIds());
        map.put("relocationEnabled", this.isRelocationEnabled());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Task;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.group = (TaskGroup) parent;
        this.title = input.readUTF();
        this.state = TaskState.fromCode(input.read());
        this.lastRunTimestamp = input.readLong();
        this.runCount = input.readLong();
        this.startAt = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(title);
        output.write(state.code());
        output.writeLong(lastRunTimestamp);
        output.writeLong(runCount);
        output.writeLong(startAt);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}

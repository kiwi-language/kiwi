package org.metavm.task;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.wire.Parent;
import org.metavm.wire.adapters.ObjectAdapter;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Constants;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Wire(adapter = ObjectAdapter.class)
@Entity
@Slf4j
public abstract class Task extends org.metavm.entity.Entity {

    public static final IndexDef<Task> IDX_STATE_LAST_RUN_AT = IndexDef.create(Task.class,
            2, task -> List.of(
                    Instances.intInstance(task.state.code()),
                    Instances.longInstance(task.lastRunTimestamp)));

    protected static final int BATCH_SIZE = 1000;

    @Getter
    private final String title;
    @Setter
    @Getter
    private TaskState state = TaskState.RUNNABLE;
    @Getter
    @Setter
    private long lastRunTimestamp;
    @Getter
    private long runCount;
    @Setter
    @Getter
    private long startAt;
    @Parent
    @Nullable
    private TaskGroup group;

    protected Task(@NotNull Id id, String title) {
        super(id);
        this.title = title;
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

    protected void onFailure(IInstanceContext taskContext) {
    }

    protected void onSuccess(IInstanceContext context, IInstanceContext taskContext) {}

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
    public void forEachChild(Consumer<? super Instance> action) {
    }

}

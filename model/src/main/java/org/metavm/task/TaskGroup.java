package org.metavm.task;

import lombok.Getter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Wire(65)
@Entity
public abstract class TaskGroup extends org.metavm.entity.Entity {

    private long completedTaskCount;
    @Getter
    private boolean failed;
    private final List<Task> tasks = new ArrayList<>();

    public TaskGroup(Id id) {
        super(id);
    }

    public void onTaskCompletion(Task task, IInstanceContext context, IInstanceContext taskContext) {
        completedTaskCount++;
        if(isCompleted()) {
            onCompletion(context, taskContext);
        }
    }

    public void onTaskFailure(Task task, IInstanceContext context, IInstanceContext taskContext) {
        failed = true;
    }

    @Override
    public void onBind(IInstanceContext context) {
        tasks.addAll(createTasks(context));
        for (var task : tasks) {
            task.setGroup(this);
        }
        if(tasks.isEmpty()) {
            onCompletion(context, context);
        }
    }

    public boolean isCompleted() {
        return completedTaskCount == tasks.size();
    }

    public boolean isTerminated() {
        return isCompleted() || isFailed();
    }

    public abstract List<Task> createTasks(IInstanceContext context);

    protected abstract void onCompletion(IInstanceContext context, IInstanceContext taskContext);

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public long getSessionTimeout() {
        return Constants.SESSION_TIMEOUT;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var tasks_ : tasks) action.accept(tasks_.getReference());
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (var tasks_ : tasks) action.accept(tasks_);
    }

}

package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Constants;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(65)
@Entity
public abstract class TaskGroup extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private long completedTaskCount;
    private boolean failed;
    private List<Task> tasks = new ArrayList<>();

    public TaskGroup() {
        System.out.println(tasks);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitBoolean();
        visitor.visitList(visitor::visitEntity);
    }

    public void onTaskCompletion(Task task, IEntityContext context, IEntityContext taskContext) {
        completedTaskCount++;
        if(isCompleted()) {
            onCompletion(context, taskContext);
        }
    }

    public void onTaskFailure(Task task, IEntityContext context, IEntityContext taskContext) {
        failed = true;
    }

    @Override
    public void onBind(IEntityContext context) {
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

    public boolean isFailed() {
        return failed;
    }

    public boolean isTerminated() {
        return isCompleted() || isFailed();
    }

    public abstract List<Task> createTasks(IEntityContext context);

    protected abstract void onCompletion(IEntityContext context, IEntityContext taskContext);

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
        tasks.forEach(arg -> action.accept(arg.getReference()));
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("completed", this.isCompleted());
        map.put("failed", this.isFailed());
        map.put("terminated", this.isTerminated());
        map.put("tasks", this.getTasks().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("sessionTimeout", this.getSessionTimeout());
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
        tasks.forEach(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_TaskGroup;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.completedTaskCount = input.readLong();
        this.failed = input.readBoolean();
        this.tasks = input.readList(() -> input.readEntity(Task.class, this));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(completedTaskCount);
        output.writeBoolean(failed);
        output.writeList(tasks, output::writeEntity);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}

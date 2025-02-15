package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(10)
public abstract class DynamicTaskGroup extends TaskGroup {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private int activeTaskCount;

    public DynamicTaskGroup(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TaskGroup.visitBody(visitor);
        visitor.visitInt();
    }

    @Override
    public void onBind(IInstanceContext context) {
        super.onBind(context);
        activeTaskCount = getTasks().size();
    }

    @Override
    public void onTaskCompletion(Task task, IInstanceContext context, IInstanceContext taskContext) {
        activeTaskCount--;
        if(getTasks().contains(task))
            super.onTaskCompletion(task, context, taskContext);
        else {
            if(isCompleted())
                onCompletion(context, taskContext);
        }
    }

    @Override
    public boolean isCompleted() {
        return activeTaskCount <= 0;
    }

    public void addTask(Task task) {
        task.setGroup(this);
        activeTaskCount++;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("completed", this.isCompleted());
        map.put("failed", this.isFailed());
        map.put("terminated", this.isTerminated());
        map.put("tasks", this.getTasks().stream().map(Entity::getStringId).toList());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_DynamicTaskGroup;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        super.readBody(input, parent);
        this.activeTaskCount = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeInt(activeTaskCount);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

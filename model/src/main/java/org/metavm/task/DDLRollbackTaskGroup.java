package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(78)
@Entity
public class DDLRollbackTaskGroup extends TaskGroup {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TaskGroup.visitBody(visitor);
    }

    @Override
    public List<Task> createTasks(IEntityContext context) {
        return List.of(new DDLRollbackTask());
    }

    @Override
    protected void onCompletion(IEntityContext context, IEntityContext taskContext) {

    }

    @Override
    public String getTitle() {
        return null;
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_DDLRollbackTaskGroup;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
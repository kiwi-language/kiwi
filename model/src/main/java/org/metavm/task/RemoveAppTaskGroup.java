package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.application.Application;
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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(7)
@Entity
public class RemoveAppTaskGroup extends TaskGroup {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Id appId;

    public RemoveAppTaskGroup(Id id, Id appId) {
        super(id);
        this.appId = appId;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TaskGroup.visitBody(visitor);
        visitor.visitId();
    }

    @Override
    public List<Task> createTasks(IInstanceContext context) {
        var app = context.getEntity(Application.class, appId);
        return List.of(
                new ClearUsersTask(nextChildId(), String.format("Clear users for '%s'", app.getName()), appId),
                new ClearInvitationTask(nextChildId(), String.format("Clear invitations for '%s'", app.getName()), appId)
        );
    }

    @Override
    protected void onCompletion(IInstanceContext context, IInstanceContext taskContext) {
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
        return EntityRegistry.TAG_RemoveAppTaskGroup;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.appId = input.readId();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeId(appId);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

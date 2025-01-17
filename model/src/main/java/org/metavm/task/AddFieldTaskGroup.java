package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(71)
@Entity
public class AddFieldTaskGroup extends TaskGroup {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference fieldReference;

    public AddFieldTaskGroup(Field field) {
        this.fieldReference = field.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TaskGroup.visitBody(visitor);
        visitor.visitValue();
    }

    public List<Task> createTasks(IInstanceContext context) {
        var field = getField();
        var klass = field.getDeclaringType();
        List<Task> tasks = new ArrayList<>();
        createTasksForKlass(klass, field, tasks);
        return tasks;
    }

    private void createTasksForKlass(Klass klass, Field field, List<Task> tasks) {
        tasks.add(new AddFieldTask(klass.getType(), field));
        for (Klass subKlass : klass.getSubKlasses()) {
            createTasksForKlass(subKlass, field, tasks);
        }
    }

    @Override
    public void onCompletion(IInstanceContext context, IInstanceContext taskContext) {
        getField().setState(MetadataState.READY);
    }

    public Field getField() {
        return (Field) fieldReference.get();
    }

    @Override
    public String getTitle() {
        return null;
    }


    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(fieldReference);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("field", this.getField().getStringId());
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
        return EntityRegistry.TAG_AddFieldTaskGroup;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.fieldReference = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(fieldReference);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

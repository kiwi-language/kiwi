package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(12)
@Entity
public class AddFieldTask extends ScanByClassTask {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference fieldReference;

    protected AddFieldTask(Id id, ClassType type, Field field) {
        super(id, "Add field", type);
        this.fieldReference = field.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ScanByClassTask.visitBody(visitor);
        visitor.visitValue();
    }

    @Override
    protected void processClassInstance(ClassInstance instance, IInstanceContext context) {
        Value fieldValue;
        var field = getField();
        if(field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType).getReference();
        else
            fieldValue = Instances.computeFieldInitialValue(instance, field, context);
        if(!instance.isFieldInitialized(field))
            instance.initField(field, fieldValue);
    }

    public Field getField() {
        return (Field) fieldReference.get();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(fieldReference);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("field", this.getField().getStringId());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_AddFieldTask;
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
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

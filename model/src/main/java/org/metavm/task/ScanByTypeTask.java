package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(50)
@Entity
public abstract class ScanByTypeTask extends ScanTask {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    protected Type type;

    protected ScanByTypeTask(Id id, String title, Type type) {
        super(id, title);
        this.type = type;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ScanTask.visitBody(visitor);
        visitor.visitValue();
    }

    @Override
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        var r = context.scan(cursor, limit);
        return new ScanResult(Utils.filter(r.instances(), this::filter), r.completed(), r.cursor());
    }

    private boolean filter(Instance instance) {
        if(type instanceof KlassType classType && classType.isTemplate()) {
            if(instance instanceof ClassInstance classInstance)
                return classInstance.getInstanceType().asSuper(classType.getKlass()) != null;
            else
                return false;
        }
        else
            return type.isInstance(instance.getReference());
    }

    @Override
    protected final void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        for (var instance : batch) {
            processInstance(instance.getReference(), context);
        }
    }

    protected abstract void processInstance(Value instance, IInstanceContext context);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
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
        var wAL = this.getWAL();
        if (wAL != null) map.put("wAL", wAL.getStringId());
        var metaWAL = this.getMetaWAL();
        if (metaWAL != null) map.put("metaWAL", metaWAL.getStringId());
        var defWalId = this.getDefWalId();
        if (defWalId != null) map.put("defWalId", defWalId);
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
        return EntityRegistry.TAG_ScanByTypeTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.type = input.readType();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(type);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

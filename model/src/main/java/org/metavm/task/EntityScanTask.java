package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.ScanResult;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@NativeEntity(75)
@Entity
public abstract class EntityScanTask<T> extends ScanTask {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private transient Class<T> entityType;
    private ClassType type;

    protected EntityScanTask(String title, Class<T> entityType) {
        super(title);
        this.entityType = entityType;
        this.type = ModelDefRegistry.getClassType(entityType);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ScanTask.visitBody(visitor);
        visitor.visitValue();
    }

    @Override
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        var r = context.scan(cursor, limit);
        return new ScanResult(
                r.instances().stream().filter(i -> type.isInstance(i.getReference())).collect(Collectors.toList()),
                r.completed(),
                r.cursor()
        );
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        List<T> models = Utils.map(
                batch, instance -> context.getEntity(entityType, instance.getId())
        );
        processModels(context, models);
    }

    protected abstract void processModels(IInstanceContext context, List<T> models);

    private void onRead() {
        //noinspection unchecked
        entityType = (Class<T>) ModelDefRegistry.getDefContext().getDef(type.getKlass()).getJavaClass();
    }

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
        return EntityRegistry.TAG_EntityScanTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.type = (ClassType) input.readType();
        this.onRead();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(type);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

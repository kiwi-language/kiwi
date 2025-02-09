package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.EntityUtils;
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

@NativeEntity(42)
@Entity
public class ReferenceCleanupTask extends Task {

    public static final long BATCH_SIZE = 256;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private Id targetId;
    private long nextTreeId;

    public ReferenceCleanupTask(Id id, Id targetId, String typeName, String instanceTitle) {
        super(id, "Reference cleanup " + instanceTitle + "/" + typeName);
        this.targetId = targetId;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitId();
        visitor.visitLong();
    }

    public Id getTargetId() {
        return targetId;
    }

    @Override
    public boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        var instanceContext = context;
        var instances = instanceContext.getByReferenceTargetId(targetId, nextTreeId, BATCH_SIZE);
        for (var instance : instances) {
            EntityUtils.ensureProxyInitialized(instance);
        }
        if(instances.size() < BATCH_SIZE) {
            return true;
        }
        nextTreeId = instances.get(instances.size() - 1).getTreeId();
        return false;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("targetId", this.getTargetId());
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
        return EntityRegistry.TAG_ReferenceCleanupTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.targetId = input.readId();
        this.nextTreeId = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeId(targetId);
        output.writeLong(nextTreeId);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

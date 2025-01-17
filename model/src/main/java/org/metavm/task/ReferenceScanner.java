package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
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

@NativeEntity(46)
@Entity
public abstract class ReferenceScanner extends Task {
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Id targetId;
    private long next;

    protected ReferenceScanner(String title, Id targetId) {
        super(title);
        this.targetId = targetId;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitId();
        visitor.visitLong();
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        var id = this.targetId;
        var referring = context.getByReferenceTargetId(id, next, BATCH_SIZE);
        if (!referring.isEmpty()) {
            process(referring);
            next = referring.get(referring.size() - 1).getTreeId() + 1;
        }
        if (referring.size() < BATCH_SIZE) {
            onTaskDone(context, id);
            return true;
        } else
            return false;
    }

    protected abstract void process(List<Instance> batch);

    protected void onTaskDone(IInstanceContext context, Id id) {
    }

    public Id getTargetId() {
        return targetId;
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
        return EntityRegistry.TAG_ReferenceScanner;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.targetId = input.readId();
        this.next = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeId(targetId);
        output.writeLong(next);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
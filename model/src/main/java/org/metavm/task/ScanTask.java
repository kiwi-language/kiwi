package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(52)
@Entity
@Slf4j
public abstract class ScanTask extends Task {

    public static final int DEFAULT_BATCH_SIZE = 256;

    public static long BATCH_SIZE = DEFAULT_BATCH_SIZE;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private long cursor;

    protected ScanTask(Id id, String title) {
        super(id, title);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitLong();
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        if(cursor == 0)
            onStart(context, taskContext);
        var r = scan(context, cursor, BATCH_SIZE);
        var batch = r.instances();
        process(batch, context, taskContext);
        if (!r.completed()) {
            cursor = r.cursor();
            return false;
        } else {
            onScanOver(context, taskContext);
            return true;
        }
    }

    protected void onStart(IInstanceContext context, IInstanceContext taskContext) {
    }

    protected void onScanOver(IInstanceContext context, IInstanceContext taskContext) {}

    protected ScanResult scan(IInstanceContext context,
                              long cursor,
                              @SuppressWarnings("SameParameterValue") long limit) {
        return context.scan(cursor, limit);
    }

    protected abstract void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
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
        return EntityRegistry.TAG_ScanTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.cursor = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeLong(cursor);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

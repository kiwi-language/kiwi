package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Constants;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(44)
public class ForwardedFlagSetter extends ReferenceScanner {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public ForwardedFlagSetter(Id id, Id targetId) {
        super(id, "MigrationMarkingTask-" + targetId, targetId);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ReferenceScanner.visitBody(visitor);
    }

    @Override
    protected void process(List<Instance> referring) {
        var id = getTargetId();
        if (!referring.isEmpty()) {
            for (Instance root : referring) {
                root.accept(new VoidInstanceVisitor() {
                    @Override
                    public Void visitInstance(Instance instance) {
                        instance.forEachChild(child -> child.accept(this));
                        instance.forEachReference(ref -> {
                            if (id.equals(ref.tryGetId()))
                                ref.setForwarded();
                        });
                        return null;
                    }
                });
            }
        }
    }

    @Override
    protected void onTaskDone(IInstanceContext context, Id id) {
        try {
            var target = (MvInstance) context.get(id);
            target.switchId();
            var redirector = new ReferenceRedirector(context.allocateRootId(), id);
            if(Constants.SESSION_TIMEOUT != -1)
                redirector.setStartAt(System.currentTimeMillis() + (Constants.SESSION_TIMEOUT << 1));
            context.bind(redirector);
        }
        catch (TreeNotFoundException ignored) {
        }
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
        return EntityRegistry.TAG_ForwardedFlagSetter;
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
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

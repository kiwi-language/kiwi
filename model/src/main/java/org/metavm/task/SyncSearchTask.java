package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@NativeEntity(73)
public class SyncSearchTask extends Task {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<Id> changedIds = new ArrayList<>();
    private List<Id> removedIds = new ArrayList<>();
    @Nullable
    private Reference walReference;
    @Nullable
    private Id defWalId;

    public SyncSearchTask(Id id, Collection<Id> changedIds, Collection<Id> removedIds, @Nullable WAL wal, @Nullable Id defWalId) {
        super(id, "SyncSearchTask");
        this.changedIds.addAll(changedIds);
        this.removedIds.addAll(removedIds);
        this.walReference = Utils.safeCall(wal, Instance::getReference);
        this.defWalId = defWalId;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitList(visitor::visitId);
        visitor.visitList(visitor::visitId);
        visitor.visitNullable(visitor::visitValue);
        visitor.visitNullable(visitor::visitId);
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        List<ClassInstance> changed = Utils.filterByType(context.batchGet(changedIds), ClassInstance.class);
        try (var ignored = context.getProfiler().enter("bulk")) {
            Hooks.SEARCH_BULK.accept(context.getAppId(), changed, removedIds);
        }
        return true;
    }

    @Nullable
    @Override
    public WAL getMetaWAL() {
        return (WAL) Utils.safeCall(walReference, Reference::get);
    }

    @Nullable
    @Override
    public Id getDefWalId() {
        return defWalId;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (walReference != null) action.accept(walReference);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        var metaWAL = this.getMetaWAL();
        if (metaWAL != null) map.put("metaWAL", metaWAL.getStringId());
        var defWalId = this.getDefWalId();
        if (defWalId != null) map.put("defWalId", defWalId);
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
        return EntityRegistry.TAG_SynchronizeSearchTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        super.readBody(input, parent);
        this.changedIds = input.readList(input::readId);
        this.removedIds = input.readList(input::readId);
        this.walReference = input.readNullable(() -> (Reference) input.readValue());
        this.defWalId = input.readNullable(input::readId);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeList(changedIds, output::writeId);
        output.writeList(removedIds, output::writeId);
        output.writeNullable(walReference, output::writeValue);
        output.writeNullable(defWalId, output::writeId);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

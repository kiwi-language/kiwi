package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.common.ErrorCode;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

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

    public SyncSearchTask(Id id, Collection<Id> changedIds, Collection<Id> removedIds) {
        super(id, "SyncSearchTask");
        this.changedIds.addAll(changedIds);
        this.removedIds.addAll(removedIds);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitList(visitor::visitId);
        visitor.visitList(visitor::visitId);
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        changedIds.forEach(context::buffer);
        var changed = new ArrayList<ClassInstance>();
        for (Id id : changedIds) {
            try {
                changed.add((ClassInstance) context.get(id));
            }
            catch (BusinessException e) {
                // It's possible that the instance is removed at this moment
                if (e.getErrorCode() != ErrorCode.INSTANCE_NOT_FOUND)
                    throw e;
            }
        }
        try (var ignored = context.getProfiler().enter("bulk")) {
            Hooks.SEARCH_BULK.accept(context.getAppId(), changed, removedIds);
        }
        return true;
    }

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
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeList(changedIds, output::writeId);
        output.writeList(removedIds, output::writeId);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

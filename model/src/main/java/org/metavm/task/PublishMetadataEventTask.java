package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.event.rest.dto.FunctionChangeEvent;
import org.metavm.event.rest.dto.TypeChangeEvent;
import org.metavm.object.instance.core.Id;
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

@NativeEntity(68)
public class PublishMetadataEventTask extends Task {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<String> changedTypeDefIds = new ArrayList<>();
    private List<String> removedTypeDefIds = new ArrayList<>();
    private List<String> changedFunctionIds = new ArrayList<>();
    private List<String> removedFunctionIds = new ArrayList<>();
    private long version;
    private @Nullable String clientId;

    public PublishMetadataEventTask(Id id,
                                    Collection<String> changedTypeDefIds,
                                    Collection<String> removedTypeDefIds,
                                    Collection<String> changedFunctionIds,
                                    Collection<String> removedFunctionIds,
                                    long version,
                                    @Nullable String clientId) {
        super(id, "PublishMetadataEventTask");
        this.changedTypeDefIds.addAll(changedTypeDefIds);
        this.removedTypeDefIds.addAll(removedTypeDefIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
        this.version = version;
        this.clientId = clientId;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Task.visitBody(visitor);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitLong();
        visitor.visitNullable(visitor::visitUTF);
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(new TypeChangeEvent(context.getAppId(),
                    version,
                    Utils.merge(changedTypeDefIds, removedTypeDefIds), clientId));
        }
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(
                    new FunctionChangeEvent(context.getAppId(), version, Utils.merge(changedFunctionIds, removedFunctionIds), clientId)
            );
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
        return EntityRegistry.TAG_PublishMetadataEventTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        super.readBody(input, parent);
        this.changedTypeDefIds = input.readList(input::readUTF);
        this.removedTypeDefIds = input.readList(input::readUTF);
        this.changedFunctionIds = input.readList(input::readUTF);
        this.removedFunctionIds = input.readList(input::readUTF);
        this.version = input.readLong();
        this.clientId = input.readNullable(input::readUTF);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeList(changedTypeDefIds, output::writeUTF);
        output.writeList(removedTypeDefIds, output::writeUTF);
        output.writeList(changedFunctionIds, output::writeUTF);
        output.writeList(removedFunctionIds, output::writeUTF);
        output.writeLong(version);
        output.writeNullable(clientId, output::writeUTF);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

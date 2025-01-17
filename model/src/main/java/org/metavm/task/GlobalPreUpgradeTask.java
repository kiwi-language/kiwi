package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.application.Application;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.PreUpgradeRequest;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(54)
public class GlobalPreUpgradeTask extends GlobalTask {

    public static TriConsumer<PreUpgradeRequest, WAL, IInstanceContext> preUpgradeAction;
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private String requestJSON;
    private Reference defWALReference;

    public GlobalPreUpgradeTask(PreUpgradeRequest preUpgradeRequest, WAL defWAL) {
        this(Utils.toJSONString(preUpgradeRequest), defWAL);
    }

    private GlobalPreUpgradeTask(String requestJSON, WAL defWAL) {
        super("GlobalPreUpgradeTask");
        this.requestJSON = requestJSON;
        this.defWALReference = defWAL.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        GlobalTask.visitBody(visitor);
        visitor.visitUTF();
        visitor.visitValue();
    }

    @Override
    protected void processApplication(IInstanceContext context, Application application) {
        preUpgradeAction.accept(getRequest(), getDefWal(), context);
    }

    public WAL getDefWal() {
        return (WAL) defWALReference.get();
    }

    private PreUpgradeRequest getRequest() {
        return Utils.readJSONString(requestJSON, PreUpgradeRequest.class);
    }

    @Override
    public long getTimeout() {
        return 3000L;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(defWALReference);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("defWal", this.getDefWal().getStringId());
        map.put("timeout", this.getTimeout());
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
        return EntityRegistry.TAG_GlobalPreUpgradeTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        super.readBody(input, parent);
        this.requestJSON = input.readUTF();
        this.defWALReference = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeUTF(requestJSON);
        output.writeValue(defWALReference);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}

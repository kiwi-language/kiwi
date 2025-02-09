package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NativeEntity(45)
@Entity
public class ShadowTask extends org.metavm.entity.Entity {

    public static final Logger logger = LoggerFactory.getLogger(ShadowTask.class);

    public static final IndexDef<ShadowTask> IDX_RUN_AT = IndexDef.create(ShadowTask.class,
            1, shadowTask -> List.of(Instances.longInstance(shadowTask.runAt)));
    public static final IndexDef<ShadowTask> IDX_EXECUTOR_IP_START_AT = IndexDef.create(ShadowTask.class,
            2, shadowTask ->
                    List.of(
                            shadowTask.executorIP != null ?
                                    Instances.stringInstance(shadowTask.executorIP) : Instances.nullInstance(),
                            Instances.longInstance(shadowTask.startAt)
                    ));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private TaskState state = TaskState.RUNNABLE;
    private @Nullable String executorIP;
    private long startAt;
    private long runAt;
    private long appId;
    private Id appTaskId;
    private @Nullable Reference defWal;

    public static BiConsumer<Long, List<Task>> saveShadowTasksHook;

    public ShadowTask(Id id, long appId, Id appTaskId, long startAt, @Nullable WAL defWal) {
        super(id);
        this.appId = appId;
        this.appTaskId = appTaskId;
        this.startAt = startAt;
        this.defWal = Utils.safeCall(defWal, Instance::getReference);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitByte();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitId();
        visitor.visitNullable(visitor::visitValue);
    }

    public long getAppId() {
        return appId;
    }

    public Id getAppTaskId() {
        return appTaskId;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getRunAt() {
        return runAt;
    }

    public void setRunAt(long runAt) {
        this.runAt = runAt;
    }

    @Nullable
    public String getExecutorIP() {
        return executorIP;
    }

    public void setExecutorIP(@Nullable String executorIP) {
        this.executorIP = executorIP;
    }

    @Nullable
    public WAL getDefWal() {
        return Utils.safeCall(defWal, r -> (WAL) r.get());
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (defWal != null) action.accept(defWal);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("appId", this.getAppId());
        map.put("appTaskId", this.getAppTaskId());
        map.put("state", this.getState().name());
        map.put("runAt", this.getRunAt());
        var executorIP = this.getExecutorIP();
        if (executorIP != null) map.put("executorIP", executorIP);
        var defWal = this.getDefWal();
        if (defWal != null) map.put("defWal", defWal.getStringId());
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
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_ShadowTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.state = TaskState.fromCode(input.read());
        this.executorIP = input.readNullable(input::readUTF);
        this.startAt = input.readLong();
        this.runAt = input.readLong();
        this.appId = input.readLong();
        this.appTaskId = input.readId();
        this.defWal = input.readNullable(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.write(state.code());
        output.writeNullable(executorIP, output::writeUTF);
        output.writeLong(startAt);
        output.writeLong(runAt);
        output.writeLong(appId);
        output.writeId(appTaskId);
        output.writeNullable(defWal, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(34)
@Entity
public class SchedulerRegistry extends org.metavm.entity.Entity {

    public static final IndexDef<SchedulerRegistry> IDX_ALL_FLAG = IndexDef.create(SchedulerRegistry.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));

    public static final long HEARTBEAT_TIMEOUT = 20000000000L;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static SchedulerRegistry getInstance(IInstanceContext context) {
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAG, Instances.trueInstance()), "SchedulerRegistry not initialized");
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(SchedulerRegistry.IDX_ALL_FLAG, Instances.trueInstance());
        if (existing != null)
            throw new IllegalStateException("SchedulerRegistry already exists");
        context.bind(new SchedulerRegistry(context.allocateRootId()));
    }

    private long version;
    private long lastHeartbeat;
    @Nullable
    private String ip;
    private boolean allFlag = true;

    public SchedulerRegistry(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitBoolean();
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Nullable
    public String getIp() {
        return ip;
    }

    public void setIp(@Nullable String ip) {
        this.ip = ip;
    }

    public boolean isHeartbeatTimeout() {
        return System.currentTimeMillis() - lastHeartbeat > HEARTBEAT_TIMEOUT;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("lastHeartbeat", this.getLastHeartbeat());
        var ip = this.getIp();
        if (ip != null) map.put("ip", ip);
        map.put("heartbeatTimeout", this.isHeartbeatTimeout());
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
        return EntityRegistry.TAG_SchedulerRegistry;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.version = input.readLong();
        this.lastHeartbeat = input.readLong();
        this.ip = input.readNullable(input::readUTF);
        this.allFlag = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(version);
        output.writeLong(lastHeartbeat);
        output.writeNullable(ip, output::writeUTF);
        output.writeBoolean(allFlag);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
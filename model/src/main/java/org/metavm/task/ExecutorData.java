package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
import org.metavm.entity.EntityRegistry;
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
import java.util.function.Consumer;

@NativeEntity(24)
@Entity
public class ExecutorData extends org.metavm.entity.Entity {

    public static final IndexDef<ExecutorData> IDX_AVAIlABLE = IndexDef.create(ExecutorData.class,
            1, executorData -> List.of(Instances.booleanInstance(executorData.available)));
    public static final IndexDef<ExecutorData> IDX_IP = IndexDef.createUnique(ExecutorData.class,
            1, executorData -> List.of(Instances.stringInstance(executorData.ip)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private String ip;
    private long lastHeartbeat;
    private boolean available;

    public ExecutorData(Id id, String ip) {
        super(id);
        this.ip = ip;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitLong();
        visitor.visitBoolean();
    }

    public String getIp() {
        return ip;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
        map.put("ip", this.getIp());
        map.put("lastHeartbeat", this.getLastHeartbeat());
        map.put("available", this.isAvailable());
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
        return EntityRegistry.TAG_ExecutorData;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.ip = input.readUTF();
        this.lastHeartbeat = input.readLong();
        this.available = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(ip);
        output.writeLong(lastHeartbeat);
        output.writeBoolean(available);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}

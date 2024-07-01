package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class SchedulerRegistry extends Entity {

    public static final IndexDef<SchedulerRegistry> IDX_ALL_FLAG = IndexDef.create(SchedulerRegistry.class, "allFlag");

    public static final long HEARTBEAT_TIMEOUT = 20000000000L;

    public static SchedulerRegistry getInstance(IEntityContext context) {
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAG, true), "SchedulerRegistry not initialized");
    }

    public static void initialize(IEntityContext context) {
        var existing = context.selectFirstByKey(SchedulerRegistry.IDX_ALL_FLAG, true);
        if (existing != null)
            throw new IllegalStateException("SchedulerRegistry already exists");
        context.bind(new SchedulerRegistry());
    }

    private long version;
    private long lastHeartbeat;
    @Nullable
    private String ip;
    private final boolean allFlag = true;

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

}
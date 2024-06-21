package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;

@EntityType
public class ExecutorData extends Entity {

    public static final IndexDef<ExecutorData> IDX_AVAIlABLE = IndexDef.create(ExecutorData.class, "available");
    public static final IndexDef<ExecutorData> IDX_IP = IndexDef.createUnique(ExecutorData.class, "ip");

    private final String ip;
    private long lastHeartbeat;
    private boolean available;

    public ExecutorData(String ip) {
        this.ip = ip;
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
}

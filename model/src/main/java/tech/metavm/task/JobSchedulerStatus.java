package tech.metavm.task;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

import javax.annotation.Nullable;

@EntityType("任务调度器状态")
public class JobSchedulerStatus extends Entity {

    public static final IndexDef<JobSchedulerStatus> IDX_ALL_FLAG = IndexDef.create(JobSchedulerStatus.class, "allFlag");

    public static final long HEARTBEAT_TIMEOUT = 20000000000L;

    @EntityField("版本")
    private long version;
    @EntityField("最近心跳时间戳")
    private long lastHeartbeat;
    @EntityField("节点IP")
    @Nullable
    private String nodeIP;
    private boolean allFlag = true;

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

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public boolean isHeartbeatTimeout() {
        return System.currentTimeMillis() - lastHeartbeat > HEARTBEAT_TIMEOUT;
    }

}

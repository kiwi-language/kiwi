package tech.metavm.object.instance.log;

import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;

public class InstanceLog {

    public static InstanceLog insert(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getInstanceId(), ChangeType.INSERT, instance.getVersion());
    }

    public static InstanceLog update(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getInstanceId(), ChangeType.UPDATE, instance.getVersion());
    }

    public static InstanceLog delete(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getInstanceId(), ChangeType.DELETE, instance.getVersion());
    }

    private final long appId;
    private final Id id;
    private final ChangeType changeType;
    private final long version;

    public InstanceLog(long appId, Id id, ChangeType changeType, long version) {
        this.appId = appId;
        this.id = id;
        this.changeType = changeType;
        this.version = version;
    }

    public long getAppId() {
        return appId;
    }

    public Id getId() {
        return id;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public VersionPO getVersion() {
        return new VersionPO(appId, id.toBytes(), version);
    }

    public boolean isInsert() {
        return changeType == ChangeType.INSERT;
    }

    public boolean isInsertOrUpdate() {
        return changeType == ChangeType.INSERT || changeType == ChangeType.UPDATE;
    }

    public boolean isDelete() {
        return changeType == ChangeType.DELETE;
    }
}

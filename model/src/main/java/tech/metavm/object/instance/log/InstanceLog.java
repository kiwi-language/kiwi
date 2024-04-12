package tech.metavm.object.instance.log;

import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.instance.persistence.VersionRT;

public class InstanceLog {

    public static InstanceLog insert(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.INSERT, version.version());
    }

    public static InstanceLog update(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.UPDATE, version.version());
    }

    public static InstanceLog delete(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.DELETE, version.version());
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
        return new VersionPO(appId, id.getPhysicalId(), version);
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

    @Override
    public String toString() {
        return "InstanceLog{" +
                "appId=" + appId +
                ", id=" + id +
                ", changeType=" + changeType +
                ", version=" + version +
                '}';
    }
}

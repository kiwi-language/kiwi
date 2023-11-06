package tech.metavm.object.instance.log;

import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;

public class InstanceLog {

    public static InstanceLog insert(InstancePO instance) {
        return new InstanceLog(instance.getTenantId(), instance.getId(), ChangeType.INSERT, instance.getVersion());
    }

    public static InstanceLog update(InstancePO instance) {
        return new InstanceLog(instance.getTenantId(), instance.getId(), ChangeType.UPDATE, instance.getVersion());
    }

    public static InstanceLog delete(VersionPO version) {
        return new InstanceLog(version.tenantId(), version.id(), ChangeType.DELETE, version.version());
    }

    private final long tenantId;
    private final long id;
    private final ChangeType changeType;
    private final long version;

    public InstanceLog(long tenantId, long id, ChangeType changeType, long version) {
        this.tenantId = tenantId;
        this.id = id;
        this.changeType = changeType;
        this.version = version;
    }

    public long getTenantId() {
        return tenantId;
    }

    public long getId() {
        return id;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public VersionPO getVersion() {
        return new VersionPO(tenantId, id, version);
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

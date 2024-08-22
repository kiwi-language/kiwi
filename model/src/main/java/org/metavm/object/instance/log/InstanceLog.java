package org.metavm.object.instance.log;

import org.metavm.api.EntityType;
import org.metavm.api.ValueObject;
import org.metavm.entity.Entity;
import org.metavm.object.instance.ChangeType;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.object.instance.persistence.VersionRT;

@EntityType
public class InstanceLog extends Entity implements ValueObject {

    public static InstanceLog insert(VersionRT version, boolean searchable) {
        return new InstanceLog(version.appId(), version.id(), searchable, ChangeType.INSERT, version.version());
    }

    public static InstanceLog update(VersionRT version, boolean searchable) {
        return new InstanceLog(version.appId(), version.id(), searchable, ChangeType.UPDATE, version.version());
    }

    public static InstanceLog delete(VersionRT version, boolean searchable) {
        return new InstanceLog(version.appId(), version.id(), searchable, ChangeType.DELETE, version.version());
    }

    private final long appId;
    private final Identifier id;
    private final boolean searchable;
    private final ChangeType changeType;
    private final long version;

    public InstanceLog(long appId, Id id, boolean searchable, ChangeType changeType, long version) {
        this.appId = appId;
        this.id = Identifier.fromId(id);
        this.searchable = searchable;
        this.changeType = changeType;
        this.version = version;
    }

    public long getAppId() {
        return appId;
    }

    public Id getId() {
        return id.toId();
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public VersionPO toVersionPO() {
        return new VersionPO(appId, getId().getTreeId(), version);
    }

    public long getVersion() {
        return version;
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

    public boolean isSearchable() {
        return searchable;
    }

    @Override
    public String toString0() {
        return "InstanceLog{" +
                "appId=" + appId +
                ", id=" + id +
                ", changeType=" + changeType +
                ", version=" + version +
                '}';
    }
}

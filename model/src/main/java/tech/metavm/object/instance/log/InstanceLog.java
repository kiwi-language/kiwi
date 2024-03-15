package tech.metavm.object.instance.log;

import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.type.TypeCategory;

public class InstanceLog {

    public static InstanceLog insert(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getId(),
                TypeTag.fromCode(instance.getTypeTag()), instance.getTypeId(), ChangeType.INSERT, instance.getVersion());
    }

    public static InstanceLog update(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getId(),
                TypeTag.fromCode(instance.getTypeTag()), instance.getTypeId(), ChangeType.UPDATE, instance.getVersion());
    }

    public static InstanceLog delete(InstancePO instance) {
        return new InstanceLog(instance.getAppId(), instance.getId(),
                TypeTag.fromCode(instance.getTypeTag()), instance.getTypeId(), ChangeType.DELETE, instance.getVersion());
    }

    private final long appId;
    private final long id;
    private final TypeTag typeTag;
    private final long typeId;
    private final ChangeType changeType;
    private final long version;

    public InstanceLog(long appId, long id, TypeTag typeTag, long typeId, ChangeType changeType, long version) {
        this.appId = appId;
        this.id = id;
        this.typeTag = typeTag;
        this.typeId = typeId;
        this.changeType = changeType;
        this.version = version;
    }

    public long getAppId() {
        return appId;
    }

    public long getId() {
        return id;
    }

    public TypeTag getTypeTag() {
        return typeTag;
    }

    public long getTypeId() {
        return typeId;
    }

    public Id getInstanceId() {
        return PhysicalId.of(id, typeTag, typeId);
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public VersionPO getVersion() {
        return new VersionPO(appId, id, version);
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

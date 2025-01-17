package org.metavm.object.instance.log;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.ChangeType;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Entity
public class InstanceLog implements ValueObject {

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;

    public static InstanceLog insert(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.INSERT, version.version(), version.entityTag());
    }

    public static InstanceLog update(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.UPDATE, version.version(), version.entityTag());
    }

    public static InstanceLog delete(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.DELETE, version.version(), version.entityTag());
    }

    private final long appId;
    private final Id id;
    private final ChangeType changeType;
    private final long version;
    private final int entityTag;

    public InstanceLog(long appId, Id id, ChangeType changeType, long version, int entityTag) {
        this.appId = appId;
        this.id = id;
        this.changeType = changeType;
        this.version = version;
        this.entityTag = entityTag;
    }

    @Generated
    public static InstanceLog read(MvInput input) {
        return new InstanceLog(input.readLong(), input.readId(), ChangeType.fromCode(input.read()), input.readLong(), input.readInt());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitId();
        visitor.visitByte();
        visitor.visitLong();
        visitor.visitInt();
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

    public VersionPO toVersionPO() {
        return new VersionPO(appId, getId().getTreeId(), version);
    }

    public long getVersion() {
        return version;
    }

    public int getEntityTag() {
        return entityTag;
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

    public void forEachReference(Consumer<Reference> action) {
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("appId", this.getAppId());
        map.put("id", this.getId());
        map.put("changeType", this.getChangeType().name());
        map.put("version", this.getVersion());
        map.put("entityTag", this.getEntityTag());
        map.put("insert", this.isInsert());
        map.put("insertOrUpdate", this.isInsertOrUpdate());
        map.put("delete", this.isDelete());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeLong(appId);
        output.writeId(id);
        output.write(changeType.code());
        output.writeLong(version);
        output.writeInt(entityTag);
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}

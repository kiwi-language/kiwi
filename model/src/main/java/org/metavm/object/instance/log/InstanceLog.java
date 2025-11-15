package org.metavm.object.instance.log;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.wire.Wire;
import org.metavm.object.instance.ChangeType;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
@Entity
public record InstanceLog(long appId, Id id, ChangeType changeType, long version,
                          int entityTag) implements ValueObject {

    public static InstanceLog insert(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.INSERT, version.version(), version.entityTag());
    }

    public static InstanceLog update(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.UPDATE, version.version(), version.entityTag());
    }

    public static InstanceLog delete(VersionRT version) {
        return new InstanceLog(version.appId(), version.id(), ChangeType.DELETE, version.version(), version.entityTag());
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

    public VersionPO toVersionPO() {
        return new VersionPO(appId, id().getTreeId(), version);
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

    public void forEachReference(Consumer<Reference> action) {
    }

    @Generated
    public void write(MvOutput output) {
        output.writeLong(appId);
        output.writeId(id);
        output.write(changeType.code());
        output.writeLong(version);
        output.writeInt(entityTag);
    }

}

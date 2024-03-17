package tech.metavm.object.instance.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.Types;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public final class PhysicalId extends Id {

    public static PhysicalId of(long id, TypeTag typeTag, long typeId) {
        return new PhysicalId(id, typeTag, typeId);
    }

    public static PhysicalId of(long id, Type type) {
        return new PhysicalId(id, type.getTag(), type.getId().getPhysicalId());
    }

    public static PhysicalId ofClass(long id, long typeId) {
        return of(id, TypeTag.Class, typeId);
    }

    public static PhysicalId ofArray(long id, long typeId) {
        return of(id, TypeTag.Array, typeId);
    }

    public static final int TAG = 1;

    private final long id;
    private final TypeTag typeTag;
    private final long typeId;

    public PhysicalId(long id, TypeTag typeTag, long typeId) {
        this.id = id;
        this.typeTag = typeTag;
        this.typeId = typeId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        output.writeId(this);
    }

    public long getId() {
        return id;
    }

    @Override
    public Long tryGetPhysicalId() {
        return id;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhysicalId that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public TypeTag getTypeTag() {
        return typeTag;
    }

    @JsonIgnore
    public long getTypeId() {
        return typeId;
    }

    @JsonIgnore
    public Id getTypeEntityId() {
        var typeType = Types.getTypeType(typeTag);
        return PhysicalId.of(typeId, TypeTag.Class, typeType.getId().getPhysicalId());
    }

}

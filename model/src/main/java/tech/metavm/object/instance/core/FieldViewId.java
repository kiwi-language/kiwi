package tech.metavm.object.instance.core;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public class FieldViewId extends PathViewId {

    public static final int TAG = 6;
    public final Id fieldId;

    public FieldViewId(ViewId parent, Id mappingId, Id fieldId, @Nullable Id sourceId, Id typeId) {
        super(parent, mappingId, sourceId, typeId);
        this.fieldId = fieldId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        getParent().write(output);
        getMappingId().write(output);
        fieldId.write(output);
        writeSourceId(output);
        getTypeId().write(output);
    }

    @Override
    public Long tryGetPhysicalId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return getParent().isTemporary();
    }

    @Override
    protected Type getViewTypeByPath(Type parentType) {
        return ((ClassType) parentType).getField(fieldId).getType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FieldViewId that)) return false;
        if (!super.equals(object)) return false;
        return fieldId == that.fieldId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fieldId);
    }
}

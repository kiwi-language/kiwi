package tech.metavm.object.instance.core;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public class FieldViewId extends PathViewId {

    public final Id fieldId;

    public FieldViewId(boolean isArray, ViewId parent, @Nullable Id mappingId, Id fieldId, @Nullable Id sourceId, TypeKey typeKey) {
        super(isArray, parent, mappingId, sourceId, typeKey);
        this.fieldId = fieldId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.FIELD_VIEW, isArray());
        getParent().write(output);
        writeMappingId(output);
        fieldId.write(output);
        writeSourceId(output);
        getTypeKey().write(output);
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
        return ((ClassType) parentType).resolve().getField(fieldId).getType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FieldViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(fieldId, that.fieldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fieldId);
    }
}

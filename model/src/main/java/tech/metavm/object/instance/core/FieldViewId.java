package tech.metavm.object.instance.core;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.rest.dto.MappingKey;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public class FieldViewId extends PathViewId {

    public final Id fieldTag;

    public FieldViewId(boolean isArray, ViewId parent, @Nullable MappingKey mappingKey, Id fieldTag, @Nullable Id sourceId, TypeKey typeKey) {
        super(isArray, parent, mappingKey, sourceId, typeKey);
        this.fieldTag = fieldTag;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.FIELD_VIEW, isArray());
        getParent().write(output);
        writeMappingKey(output);
        fieldTag.write(output);
        writeSourceId(output);
        getTypeKey().write(output);
    }

    @Override
    public Long tryGetTreeId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return getParent().isTemporary();
    }

    @Override
    protected Type getViewTypeByPath(Type parentType) {
        return ((ClassType) parentType).resolve().getField(f -> f.getEffectiveTemplate().idEquals(fieldTag)).getType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FieldViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(fieldTag, that.fieldTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fieldTag);
    }
}

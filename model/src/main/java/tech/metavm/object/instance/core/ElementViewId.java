package tech.metavm.object.instance.core;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public class ElementViewId extends PathViewId {

    private final int index;

    public ElementViewId(boolean isArray, ViewId parent, @Nullable Id mappingId, int index, @Nullable Id sourceId, TypeKey type) {
        super(isArray, parent, mappingId, sourceId, type);
        this.index = index;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.ELEMENT_VIEW, isArray());
        getParent().write(output);
        writeMappingId(output);
        output.writeInt(index);
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
        return ((ArrayType) parentType).getElementType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ElementViewId that)) return false;
        if (!super.equals(object)) return false;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index);
    }
}

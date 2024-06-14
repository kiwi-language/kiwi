package org.metavm.object.instance.core;

import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public class ElementViewId extends PathViewId {

    private final int index;

    public ElementViewId(boolean isArray, ViewId parent, @Nullable MappingKey mappingKey, int index, @Nullable Id sourceId, TypeKey type) {
        super(isArray, parent, mappingKey, sourceId, type);
        this.index = index;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.ELEMENT_VIEW, isArray());
        getParent().write(output);
        writeMappingKey(output);
        output.writeInt(index);
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

package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.view.MappingProvider;
import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;

import java.util.Objects;

public abstract class PathViewId extends ViewId {

    private final ViewId parent;

    private final @Nullable Id sourceId;

    private final TypeKey typeKey;

    protected PathViewId(boolean isArray, ViewId parent, @Nullable MappingKey mappingKey, @Nullable Id sourceId, TypeKey typeKey) {
        super(isArray, mappingKey);
        this.parent = parent;
        this.sourceId = sourceId;
        this.typeKey = typeKey;
    }

    public Id getParent() {
        return parent;
    }

    public @Nullable Id getSourceId() {
        return sourceId;
    }

    public void writeSourceId(InstanceOutput output) {
        if(sourceId != null) {
            output.writeBoolean(true);
            sourceId.write(output);
        }
        else
            output.writeBoolean(false);
    }

    public static @Nullable Id readSourceId(InstanceInput input) {
        var hasSource = input.readBoolean();
        return hasSource ? Id.readId(input) : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PathViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(typeKey, that.typeKey) && Objects.equals(parent, that.parent) && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent, sourceId, typeKey);
    }

    @Override
    public ViewId getRootId() {
        return parent.getRootId();
    }

    @Nullable
    @Override
    public SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        var mappingKey = getMappingKey();
        if(sourceId != null)
            return new SourceRef(instanceProvider.get(sourceId).getReference(), mappingKey != null ? mappingKey.toMapping(mappingProvider, typeDefProvider) : null);
        else
            return null;
    }

    @Override
    public TypeKey getViewTypeKey(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        var mappingId = getMappingKey();
        if(mappingId != null)
            return super.getViewTypeKey(mappingProvider, typeDefProvider);
        return typeKey;
    }

    protected abstract Type getViewTypeByPath(Type parentType);

    public TypeKey getTypeKey() {
        return typeKey;
    }
}

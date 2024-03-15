package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public abstract class PathViewId extends ViewId {

    private final ViewId parent;

    private final @Nullable Id sourceId;

    private final Id typeId;

    protected PathViewId(ViewId parent, Id mappingId, @Nullable Id sourceId, Id typeId) {
        super(mappingId);
        this.parent = parent;
        this.sourceId = sourceId;
        this.typeId = typeId;
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
        return typeId == that.typeId && Objects.equals(parent, that.parent) && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent, sourceId, typeId);
    }

    @Override
    public ViewId getRootId() {
        return parent.getRootId();
    }

    @Nullable
    @Override
    public SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider) {
        var mappingId = getMappingId();
        if(sourceId != null)
            return new SourceRef(instanceProvider.get(sourceId), mappingId != null ? mappingProvider.getMapping(mappingId) : null);
        else
            return null;
    }

    @Override
    public Type getViewType(MappingProvider mappingProvider, TypeProvider typeProvider) {
        var mappingId = getMappingId();
        if(mappingId != null)
            return super.getViewType(mappingProvider, typeProvider);
        return typeProvider.getType(typeId);
//        return getViewTypeByPath(parent.getViewType(mappingProvider, typeProvider));
    }

    protected abstract Type getViewTypeByPath(Type parentType);

    public Id getTypeId() {
        return typeId;
    }
}

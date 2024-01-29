package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public abstract class PathViewId extends ViewId {

    private final ViewId parent;

    private final @Nullable Id sourceId;

    protected PathViewId(ViewId parent, long mappingId, @Nullable Id sourceId) {
        super(mappingId);
        this.parent = parent;
        this.sourceId = sourceId;
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
        return Objects.equals(parent, that.parent) && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent, sourceId);
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
            return new SourceRef(instanceProvider.get(sourceId), mappingId > 0 ? mappingProvider.getMapping(mappingId) : null);
        else
            return null;
    }

    @Override
    public Type getViewType(MappingProvider mappingProvider) {
        var mappingId = getMappingId();
        if(mappingId > 0L)
            return super.getViewType(mappingProvider);
        return getViewTypeByPath(parent.getViewType(mappingProvider));
    }

    protected abstract Type getViewTypeByPath(Type parentType);

}

package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;
import tech.metavm.object.view.MappingProvider;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ViewId extends Id {

    private final long mappingId;

    public ViewId(long mappingId) {
        this.mappingId = mappingId;
    }

    public long getMappingId() {
        return mappingId;
    }

    public abstract ViewId getRootId();

    public abstract @Nullable SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider);

    public Type getViewType(MappingProvider mappingProvider) {
        return mappingProvider.getMapping(mappingId).getTargetType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ViewId viewId)) return false;
        return mappingId == viewId.mappingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingId);
    }
}

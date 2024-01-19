package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.view.MappingProvider;

import java.util.Objects;

public abstract class PathViewId extends ViewId {

    private final ViewId parent;

    protected PathViewId(ViewId parent, long mappingId) {
        super(mappingId);
        this.parent = parent;
    }

    public Id getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PathViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent);
    }

    @Override
    public ViewId getRootId() {
        return parent.getRootId();
    }

    @Nullable
    @Override
    public SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider) {
        return null;
    }
}

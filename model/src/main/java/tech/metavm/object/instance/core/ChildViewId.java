package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class ChildViewId extends DefaultViewId {

    private final ViewId rootId;

    public ChildViewId(boolean isArray, Id mappingId, Id sourceId, ViewId rootId) {
        super(isArray, mappingId, sourceId);
        this.rootId = rootId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.CHILD_VIEW, isArray());
        getMappingId().write(output);
        getSourceId().write(output);
        rootId.write(output);
    }

    @Override
    public ViewId getRootId() {
        return rootId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ChildViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(rootId, that.rootId);
    }



    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rootId);
    }

}

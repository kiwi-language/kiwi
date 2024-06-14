package org.metavm.object.instance.core;

import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.util.InstanceOutput;

import java.util.Objects;

public class ChildViewId extends DefaultViewId {

    private final ViewId rootId;

    public ChildViewId(boolean isArray, MappingKey mappingKey, Id sourceId, ViewId rootId) {
        super(isArray, mappingKey, sourceId);
        this.rootId = rootId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.CHILD_VIEW, isArray());
        getMappingKey().write(output);
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

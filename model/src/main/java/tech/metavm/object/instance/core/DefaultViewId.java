package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

public class DefaultViewId extends ViewId {

    public static final int TAG = 3;

    private final Id sourceId;

    public DefaultViewId(Id mappingId, Id sourceId) {
        super(mappingId);
        this.sourceId = sourceId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TAG);
        getMappingId().write(output);
        sourceId.write(output);
    }

    public Id getSourceId() {
        return sourceId;
    }


    @Override
    public Long tryGetPhysicalId() {
        return sourceId.tryGetPhysicalId();
    }

    @Override
    public boolean isTemporary() {
        return sourceId.isTemporary();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof DefaultViewId that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourceId);
    }

    @Override
    public ViewId getRootId() {
        return this;
    }

    @Nullable
    @Override
    public SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider) {
        return new SourceRef(instanceProvider.get(sourceId), mappingProvider.getMapping(getMappingId()));
    }

}

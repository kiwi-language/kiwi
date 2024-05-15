package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.object.view.rest.dto.MappingKey;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DefaultViewId extends ViewId {

    private final Id sourceId;

    public DefaultViewId(boolean isArray, @NotNull MappingKey mappingKey, Id sourceId) {
        super(isArray, mappingKey);
        this.sourceId = sourceId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.DEFAULT_VIEW, isArray());
        getMappingKey().write(output);
        sourceId.write(output);
    }

    @Override
    public @NotNull MappingKey getMappingKey() {
        return requireNonNull(super.getMappingKey());
    }

    public Id getSourceId() {
        return sourceId;
    }


    @Override
    public Long tryGetTreeId() {
        return sourceId.tryGetTreeId();
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
    public SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return new SourceRef(instanceProvider.get(sourceId), getMappingKey().toMapping(mappingProvider, typeDefProvider));
    }

}

package tech.metavm.object.instance.core;

import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ViewId extends Id {

    private final @Nullable Id mappingId;

    public ViewId(boolean isArray, @Nullable Id mappingId) {
        super(isArray);
        this.mappingId = mappingId;
    }

    public @Nullable Id getMappingId() {
        return mappingId;
    }

    public abstract ViewId getRootId();

    public abstract @Nullable SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider);

    public TypeKey getViewTypeKey(MappingProvider mappingProvider) {
        return mappingProvider.getMapping(mappingId).getTargetType().toTypeKey();
    }

    public void writeMappingId(InstanceOutput output) {
        if(mappingId != null) {
            output.writeBoolean(true);
            mappingId.write(output);
        }
        else
            output.writeBoolean(false);
    }

    public static @Nullable Id readMappingId(InstanceInput input) {
        var hasMapping = input.readBoolean();
        return hasMapping ? Id.readId(input) : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ViewId viewId)) return false;
        return Objects.equals(mappingId, viewId.mappingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingId);
    }
}

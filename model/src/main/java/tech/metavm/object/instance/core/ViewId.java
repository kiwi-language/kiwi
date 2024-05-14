package tech.metavm.object.instance.core;

import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.object.view.rest.dto.MappingKey;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ViewId extends Id {

    private final @Nullable MappingKey mappingKey;

    public ViewId(boolean isArray, @Nullable MappingKey mappingKey) {
        super(isArray);
        this.mappingKey = mappingKey;
    }

    public @Nullable MappingKey getMappingKey() {
        return mappingKey;
    }

    public abstract ViewId getRootId();

    public abstract @Nullable SourceRef getSourceRef(InstanceProvider instanceProvider, MappingProvider mappingProvider, TypeDefProvider typeDefProvider);

    public TypeKey getViewTypeKey(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return Objects.requireNonNull(mappingKey).toMapping(mappingProvider, typeDefProvider).getTargetType().toTypeKey();
    }

    public void writeMappingKey(InstanceOutput output) {
        if(mappingKey != null) {
            output.writeBoolean(true);
            mappingKey.write(output);
        }
        else
            output.writeBoolean(false);
    }

    public static @Nullable MappingKey readMappingKey(InstanceInput input) {
        var hasMapping = input.readBoolean();
        return hasMapping ? MappingKey.read(input) : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ViewId viewId)) return false;
        return Objects.equals(mappingKey, viewId.mappingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingKey);
    }
}

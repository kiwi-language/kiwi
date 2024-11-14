package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.flow.NodeRT;
import org.metavm.flow.Nodes;
import org.metavm.flow.Code;
import org.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public class ObjectNestedMapping extends NestedMapping {

    private final @NotNull ObjectMappingRef mappingRef;

    public ObjectNestedMapping(@NotNull ObjectMappingRef mappingRef) {
        this.mappingRef = mappingRef;
    }

    @Override
    public Type generateMappingCode(Supplier<NodeRT> getSource, Code code) {
        getSource.get();
        var mapping = mappingRef.resolve();
        Nodes.map(code, mapping);
        return mapping.getTargetType();
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, Code code) {
        viewSupplier.get();
        var mapping = mappingRef.resolve();
        Nodes.unmap(code, mapping);
        return mapping.getSourceType();
    }

    public ObjectMapping getMapping() {
        return mappingRef.resolve();
    }

    @Override
    public Type getTargetType() {
        return mappingRef.resolve().targetType;
    }

    @Override
    public String getText() {
        return "{\"kind\": \"Object\"}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ObjectNestedMapping that)) return false;
        return Objects.equals(mappingRef, that.mappingRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingRef);
    }
}

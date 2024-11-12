package org.metavm.object.view;

import org.metavm.api.EntityType;
import org.metavm.flow.NodeRT;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public class IdentityNestedMapping extends NestedMapping {

    private final Type type;

    public IdentityNestedMapping(Type type) {
        this.type = type;
    }

    @Override
    public Type generateMappingCode(Supplier<NodeRT> getSource, ScopeRT scope) {
        getSource.get();
        return type;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, ScopeRT scope) {
        viewSupplier.get();
        return type;
    }

    @Override
    public Type getTargetType() {
        return type;
    }

    @Override
    public String getText() {
        return "{\"kind\": \"Identity\"}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof IdentityNestedMapping that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}

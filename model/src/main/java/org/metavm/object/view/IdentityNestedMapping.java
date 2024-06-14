package org.metavm.object.view;

import org.metavm.entity.EntityType;
import org.metavm.flow.ScopeRT;
import org.metavm.flow.Value;
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
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        return getSource;
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        return getView;
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

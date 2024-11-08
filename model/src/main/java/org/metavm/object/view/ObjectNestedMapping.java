package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.flow.Nodes;
import org.metavm.flow.ScopeRT;
import org.metavm.flow.Value;
import org.metavm.flow.Values;
import org.metavm.object.type.Type;

import java.util.Objects;

@EntityType
public class ObjectNestedMapping extends NestedMapping {

    private final @NotNull ObjectMappingRef mappingRef;

    public ObjectNestedMapping(@NotNull ObjectMappingRef mappingRef) {
        this.mappingRef = mappingRef;
    }

    @Override
    public Value generateMappingCode(Value source, ScopeRT scope) {
        var mapNode = Nodes.map(scope.nextNodeName("nestedMapping"), scope, source, mappingRef.resolve());
        return Values.node(mapNode);
    }

    @Override
    public Value generateUnmappingCode(Value view, ScopeRT scope) {
        var source = Nodes.unmap(scope.nextNodeName("unmap"), scope, view, mappingRef.resolve());
        return Values.node(source);
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

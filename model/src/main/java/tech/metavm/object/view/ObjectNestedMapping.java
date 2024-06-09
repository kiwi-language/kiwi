package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Nodes;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.Value;
import tech.metavm.flow.Values;
import tech.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public class ObjectNestedMapping extends NestedMapping {

    private final @NotNull ObjectMappingRef mappingRef;

    public ObjectNestedMapping(@NotNull ObjectMappingRef mappingRef) {
        this.mappingRef = mappingRef;
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var mapNode = Nodes.map(scope.nextNodeName("nestedMapping"), scope, getSource.get(), mappingRef.resolve());
        return () -> Values.node(mapNode);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
//        if (sourceType == targetType)
//            return getView;
        var source = Nodes.unmap(scope.nextNodeName("unmap"), scope, getView.get(), mappingRef.resolve());
        return () -> Values.node(source);
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

package tech.metavm.object.view;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.Value;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType("自身嵌套映射")
public class IdentityNestedMapping extends NestedMapping {

    @EntityField("类型")
    private final Type type;

    public IdentityNestedMapping(Type type) {
        this.type = type;
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope, CompositeTypeFacade compositeTypeFacade) {
        return getSource;
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope, CompositeTypeFacade compositeTypeFacade) {
        return getView;
    }

    @Override
    public Type getTargetType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof IdentityNestedMapping that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}

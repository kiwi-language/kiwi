package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@EntityType
public abstract class CompositeType extends Type {

    public CompositeType() {
        super();
    }

    public abstract List<Type> getComponentTypes();

    @Override
    public boolean isUncertain() {
        return NncUtils.anyMatch(getComponentTypes(), Type::isUncertain);
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return NncUtils.flatMapUnique(getComponentTypes(), Type::getVariables);
    }

    @Override
    public boolean isCaptured() {
        return NncUtils.anyMatch(getComponentTypes(), Type::isCaptured);
    }

    @Override
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        getComponentTypes().forEach(t -> t.getCapturedTypes(capturedTypes));
    }

    @Override
    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
        getComponentTypes().forEach(t -> t.accept(visitor, s));
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        getComponentTypes().forEach(t -> t.forEachTypeDef(action));
    }
}

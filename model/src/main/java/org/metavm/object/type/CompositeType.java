package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Entity
public abstract class CompositeType extends Type {

    public CompositeType() {
        super();
    }

    public abstract List<Type> getComponentTypes();

    @Override
    public boolean isUncertain() {
        return Utils.anyMatch(getComponentTypes(), Type::isUncertain);
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return Utils.flatMapUnique(getComponentTypes(), Type::getVariables);
    }

    @Override
    public boolean isCaptured() {
        return Utils.anyMatch(getComponentTypes(), Type::isCaptured);
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCompositeType(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }
}

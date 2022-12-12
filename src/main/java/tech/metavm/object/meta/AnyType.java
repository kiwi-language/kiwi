package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;

@EntityType("任意类型")
public class AnyType extends Type {

    public AnyType() {
        super("Any",  false, false, TypeCategory.ANY);
    }

    @Override
    public Type getConcreteType() {
        return this;
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return true;
    }

    @Override
    protected Object getParam() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyType;
    }

    @Override
    public int hashCode() {
        return AnyType.class.hashCode();
    }

}

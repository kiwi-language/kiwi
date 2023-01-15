package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;

import java.util.function.Function;

@EntityType("任意类型")
public class AnyType extends Type {

    public AnyType() {
        super("任意类型",  false, false, TypeCategory.ANY);
        setCode("Any");
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
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "Any";
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

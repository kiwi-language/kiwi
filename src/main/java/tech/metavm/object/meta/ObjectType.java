package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;

import java.util.function.Function;

@EntityType("Object类型")
public class ObjectType extends Type {

    public ObjectType() {
        super("对象类型",  false, false, TypeCategory.OBJECT);
        setCode("Object");
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
        return obj instanceof ObjectType;
    }

    @Override
    public int hashCode() {
        return ObjectType.class.hashCode();
    }

}

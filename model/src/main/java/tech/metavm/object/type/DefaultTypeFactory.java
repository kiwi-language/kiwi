package tech.metavm.object.type;

import java.util.function.Function;

public class DefaultTypeFactory extends TypeFactory {

    private final Function<java.lang.reflect.Type, Type> getTypeFunc;

    public DefaultTypeFactory(Function<java.lang.reflect.Type, Type> getTypeFunc) {
        this.getTypeFunc = getTypeFunc;
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return getTypeFunc.apply(javaType);
    }

}

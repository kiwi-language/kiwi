package tech.metavm.entity;

import tech.metavm.object.type.Type;

import java.util.function.Function;

public class PrimitiveInstanceFactory {
    
    private final Function<Class<?>, Type> getType;

    public PrimitiveInstanceFactory(Function<Class<?>, Type> getType) {
        this.getType = getType;
    }


}

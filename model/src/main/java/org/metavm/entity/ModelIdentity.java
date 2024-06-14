package org.metavm.entity;


import java.lang.reflect.Type;

public record ModelIdentity(
        Type type,
        String name,
        boolean relative
) {

    public String qualifiedName() {
        return type.getTypeName() + "." + name;
    }

    @Override
    public String toString() {
        return "ModelId [" + name + ":" + type.getTypeName() + "]";
    }
}

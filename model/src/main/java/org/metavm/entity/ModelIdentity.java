package org.metavm.entity;


import java.lang.reflect.Type;

public record ModelIdentity(
        Type type,
        String name,
        boolean relative
) {

    public static ModelIdentity create(Type type, String name) {
        return new ModelIdentity(type, name, false);
    }

    public String qualifiedName() {
        return type.getTypeName() + "." + name;
    }

    @Override
    public String toString() {
        return "ModelId [" + name + ":" + type.getTypeName() + "]";
    }
}

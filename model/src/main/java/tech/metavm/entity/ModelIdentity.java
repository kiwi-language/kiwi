package tech.metavm.entity;


import java.lang.reflect.Type;

public record ModelIdentity(
        Type type,
        String name,
        boolean relative
) {

    @Override
    public String toString() {
        return "ModelId [" + name + ":" + type.getTypeName() + "]";
    }
}

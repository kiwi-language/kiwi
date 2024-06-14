package org.metavm.object.instance.core;

public record TypeId(
        TypeTag tag,
        long id
) {

    public static TypeId ofArray(long id) {
        return new TypeId(TypeTag.ARRAY, id);
    }

    public static TypeId ofClass(long id) {
        return new TypeId(TypeTag.CLASS, id);
    }
}

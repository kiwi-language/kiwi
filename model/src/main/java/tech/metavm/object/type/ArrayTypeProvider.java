package tech.metavm.object.type;

import javax.annotation.Nullable;

public interface ArrayTypeProvider {

    ArrayType getArrayType(Type elementType, ArrayKind kind, @Nullable Long tmpId);

    default ArrayType getArrayType(Type elementType, ArrayKind kind) {
        return getArrayType(elementType, kind, null);
    }

}

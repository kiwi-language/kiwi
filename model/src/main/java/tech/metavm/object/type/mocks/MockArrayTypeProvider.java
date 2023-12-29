package tech.metavm.object.type.mocks;

import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.Type;

import java.util.HashMap;
import java.util.Map;

public class MockArrayTypeProvider implements ArrayTypeProvider {

    private final Map<Key, ArrayType> map = new HashMap<>();

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind, Long tmpId) {
        return map.computeIfAbsent(new Key(elementType, kind),
                k -> new ArrayType(tmpId , elementType, kind));
    }

    private record Key(Type elementType, ArrayKind kind) {}

}

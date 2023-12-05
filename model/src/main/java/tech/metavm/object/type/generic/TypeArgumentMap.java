package tech.metavm.object.type.generic;

import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeVariable;

import java.util.HashMap;
import java.util.Map;

public class TypeArgumentMap {

    public static final TypeArgumentMap EMPTY = new TypeArgumentMap(Map.of());

    private final Map<TypeVariable, Type> map;

    public TypeArgumentMap(Map<TypeVariable, ? extends Type> map) {
        this.map = new HashMap<>(map);
    }

    public Type get(TypeVariable typeVariable) {
        return map.getOrDefault(typeVariable, typeVariable);
    }

    public TypeArgumentMap merge(TypeArgumentMap that) {
        return merge(that.map);
    }

    public TypeArgumentMap merge(Map<TypeVariable, Type> map) {
        var mergedMap = new HashMap<>(this.map);
        mergedMap.putAll(map);
        return new TypeArgumentMap(mergedMap);
    }


}

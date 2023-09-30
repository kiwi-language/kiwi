package tech.metavm.object.meta.generic;

import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeVariable;

import java.util.HashMap;
import java.util.Map;

public class MetaSubstitutor {

    public static final MetaSubstitutor EMPTY = new MetaSubstitutor(Map.of());

    private final Map<TypeVariable, Type> map;

    public MetaSubstitutor(Map<TypeVariable, Type> map) {
        this.map = new HashMap<>(map);
    }

    public Type substitute(TypeVariable typeVariable) {
        return map.getOrDefault(typeVariable, typeVariable);
    }

    public MetaSubstitutor merge(MetaSubstitutor that) {
        return merge(that.map);
    }

    public MetaSubstitutor merge(Map<TypeVariable, Type> map) {
        var mergedMap = new HashMap<>(this.map);
        mergedMap.putAll(map);
        return new MetaSubstitutor(mergedMap);
    }


}

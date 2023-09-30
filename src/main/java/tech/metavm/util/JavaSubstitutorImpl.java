package tech.metavm.util;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JavaSubstitutorImpl implements JavaSubstitutor {

    public static final JavaSubstitutor EMPTY = new JavaSubstitutorImpl(Map.of());

    private final Map<TypeVariable<?>, Type> map;
    private final Transformer transformer = new Transformer();

    public JavaSubstitutorImpl(Map<TypeVariable<?>, Type> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    public Type substitute(Type type) {
        return transformer.transformType(type);
    }

    @Override
    public Map<TypeVariable<?>, Type> toMap() {
        return Collections.unmodifiableMap(map);
    }

    private class Transformer extends TypeTransformer {
        @Override
        public Type transformTypeVariable(TypeVariable<?> typeVariable) {
            return map.getOrDefault(typeVariable, typeVariable);
        }
    }

    public JavaSubstitutor merge(JavaSubstitutor substitutor) {
        return merge(substitutor.toMap());
    }

    public JavaSubstitutor merge(Map<TypeVariable<?>, Type> map) {
        Map<TypeVariable<?>, Type> mergedMap = new HashMap<>(this.map);
        mergedMap.putAll(map);
        return new JavaSubstitutorImpl(mergedMap);
    }

}

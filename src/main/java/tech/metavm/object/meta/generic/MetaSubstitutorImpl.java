package tech.metavm.object.meta.generic;

import tech.metavm.object.meta.Type;

import java.util.IdentityHashMap;
import java.util.Map;

public class MetaSubstitutorImpl implements MetaSubstitutor{

    private final GenericContext genericContext;
    private final TypeArgumentMap typeArgumentMap;
    private final Map<Type, Type> cache = new IdentityHashMap<>();

    public MetaSubstitutorImpl(GenericContext genericContext, TypeArgumentMap typeArgumentMap) {
        this.genericContext = genericContext;
        this.typeArgumentMap = typeArgumentMap;
    }

    @Override
    public Type substitute(Type type) {
        return cache.computeIfAbsent(type, t -> genericContext.substitute(t, typeArgumentMap));
    }
}

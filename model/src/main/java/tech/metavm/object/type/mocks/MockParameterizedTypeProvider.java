package tech.metavm.object.type.mocks;

import tech.metavm.object.type.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockParameterizedTypeProvider implements ParameterizedTypeProvider {

    private final Map<Key, ClassType> map = new HashMap<>();
    private final TypeProviders typeProviders;

    public MockParameterizedTypeProvider(TypeProviders typeProviders) {
        this.typeProviders = typeProviders;
    }

    @Override
    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        return map.computeIfAbsent(new Key(template, typeArguments),
                k -> createParameterizedType(template, typeArguments, stage));
    }

    private ClassType createParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage) {
        var subst = typeProviders.createSubstitutor(template, template.getTypeParameters(), typeArguments, stage);
        return (ClassType) subst.visitClassType(template);
    }

    @Override
    public ClassType getExisting(ClassType template, List<? extends Type> typeArguments) {
        return map.get(new Key(template, typeArguments));
    }

    @Override
    public void add(ClassType parameterizedType) {
        map.put(new Key(parameterizedType.getEffectiveTemplate(), parameterizedType.getTypeArguments()), parameterizedType);
    }

    private record Key(ClassType template, List<? extends Type> typeArguments) {
    }
}

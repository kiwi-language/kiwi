package tech.metavm.object.type.mocks;

import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockParameterizedTypeRepository implements ParameterizedTypeRepository {

    private final Map<Key, Klass> map = new HashMap<>();
    private final TypeProviders typeProviders;

    public MockParameterizedTypeRepository(TypeProviders typeProviders) {
        this.typeProviders = typeProviders;
    }

    @Override
    public Klass getParameterizedType(Klass template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        var key = new Key(template, typeArguments);
        var pType = map.get(key);
        if(pType == null) {
            pType = createParameterizedType(template, typeArguments, stage);
            map.put(key, pType);
        }
        return pType;
    }

    @Override
    public List<Klass> getTemplateInstances(Klass template) {
        return NncUtils.filter(map.values(), t -> t.getTemplate() == template);
    }

    private Klass createParameterizedType(Klass template, List<? extends Type> typeArguments, ResolutionStage stage) {
        var subst = typeProviders.createSubstitutor(template, template.getTypeParameters(), typeArguments, stage);
        return (Klass) subst.visitKlass(template);
    }

    @Override
    public Klass getExisting(Klass template, List<? extends Type> typeArguments) {
        return map.get(new Key(template, typeArguments));
    }

    @Override
    public void add(Klass parameterizedType) {
        map.put(new Key(parameterizedType.getEffectiveTemplate(), parameterizedType.getTypeArguments()), parameterizedType);
    }

    private record Key(Klass template, List<? extends Type> typeArguments) {
    }
}

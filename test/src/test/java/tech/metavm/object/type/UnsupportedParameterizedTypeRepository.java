package tech.metavm.object.type;

import java.util.List;

public class UnsupportedParameterizedTypeRepository implements ParameterizedTypeRepository {

    @Override
    public Klass getParameterizedType(Klass template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Klass> getTemplateInstances(Klass template) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Klass getExisting(Klass template, List<? extends Type> typeArguments) {
        return null;
    }

    @Override
    public void add(Klass parameterizedType) {
    }

}

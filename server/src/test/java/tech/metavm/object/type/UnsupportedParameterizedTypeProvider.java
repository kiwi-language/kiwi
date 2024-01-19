package tech.metavm.object.type;

import java.util.List;

public class UnsupportedParameterizedTypeProvider implements ParameterizedTypeProvider {

    @Override
    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassType getExisting(ClassType template, List<? extends Type> typeArguments) {
        return null;
    }

    @Override
    public void add(ClassType parameterizedType) {
    }

}

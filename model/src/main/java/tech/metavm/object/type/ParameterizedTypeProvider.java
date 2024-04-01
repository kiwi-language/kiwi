package tech.metavm.object.type;

import java.util.List;

public interface ParameterizedTypeProvider {

    default ClassType getParameterizedType(ClassType template,
                                           List<? extends Type> typeArguments) {
        return getParameterizedType(template, typeArguments, ResolutionStage.DEFINITION, new MockDTOProvider());
    }

    ClassType getParameterizedType(ClassType template,
                                   List<? extends Type> typeArguments,
                                   ResolutionStage stage,
                                   DTOProvider dtoProvider
    );
}

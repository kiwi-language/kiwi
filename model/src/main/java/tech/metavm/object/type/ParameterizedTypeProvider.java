package tech.metavm.object.type;

import java.util.List;

public interface ParameterizedTypeProvider {

    default Klass getParameterizedType(Klass template,
                                       List<? extends Type> typeArguments) {
        return getParameterizedType(template, typeArguments, ResolutionStage.DEFINITION, new MockDTOProvider());
    }

    Klass getParameterizedType(Klass template,
                               List<? extends Type> typeArguments,
                               ResolutionStage stage,
                               DTOProvider dtoProvider
    );
}

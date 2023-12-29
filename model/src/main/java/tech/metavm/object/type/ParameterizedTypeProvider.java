package tech.metavm.object.type;

import java.util.List;

public interface ParameterizedTypeProvider {

    ClassType getParameterizedType(ClassType template,
                                   List<? extends Type> typeArguments,
                                   ResolutionStage stage,
                                   DTOProvider dtoProvider
    );

    ClassType getExisting(ClassType template, List<? extends Type> typeArguments);

    void add(ClassType parameterizedType);
}

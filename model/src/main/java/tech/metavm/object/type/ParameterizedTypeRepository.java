package tech.metavm.object.type;

import java.util.List;

public interface ParameterizedTypeRepository extends ParameterizedTypeProvider {

    List<ClassType> getTemplateInstances(ClassType template);

    ClassType getExisting(ClassType template, List<? extends Type> typeArguments);

    void add(ClassType parameterizedType);
}

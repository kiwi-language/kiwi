package tech.metavm.object.type;

import java.util.List;

public interface ParameterizedTypeRepository extends ParameterizedTypeProvider {

    List<Klass> getTemplateInstances(Klass template);

    Klass getExisting(Klass template, List<? extends Type> typeArguments);

    void add(Klass parameterizedType);
}

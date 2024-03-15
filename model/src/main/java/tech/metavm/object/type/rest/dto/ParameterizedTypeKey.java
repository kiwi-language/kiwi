package tech.metavm.object.type.rest.dto;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public record ParameterizedTypeKey(String templateId, List<String> typeArgumentIds) implements TypeKey{

    public static ParameterizedTypeKey create(ClassType template, List<Type> typeArguments) {
        return new ParameterizedTypeKey(template.getEntityId().toString(), NncUtils.map(typeArguments, e -> e.getEntityId().toString()));
    }

}

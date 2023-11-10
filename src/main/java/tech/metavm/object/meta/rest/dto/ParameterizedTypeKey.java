package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.Entity;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public record ParameterizedTypeKey(RefDTO templateRef, List<RefDTO> typeArgumentRefs) implements TypeKey{

    public static ParameterizedTypeKey create(ClassType template, List<Type> typeArguments) {
        return new ParameterizedTypeKey(template.getRef(), NncUtils.map(typeArguments, Entity::getRef));
    }

}

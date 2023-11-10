package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

public record UncertainTypeKey(RefDTO lowerBoundRef, RefDTO upperBoundRef) implements TypeKey{
}

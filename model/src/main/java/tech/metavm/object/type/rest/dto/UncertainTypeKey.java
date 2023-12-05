package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

public record UncertainTypeKey(RefDTO lowerBoundRef, RefDTO upperBoundRef) implements TypeKey{
}

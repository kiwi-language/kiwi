package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.Set;

public record IntersectionTypeKey(Set<RefDTO> typeRefs) implements TypeKey{
}

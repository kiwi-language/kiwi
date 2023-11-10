package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;
import java.util.Set;

public record IntersectionTypeKey(Set<RefDTO> typeRefs) implements TypeKey{
}

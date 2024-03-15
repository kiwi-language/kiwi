package tech.metavm.object.type.rest.dto;

import java.util.Set;

public record IntersectionTypeKey(Set<String> typeIds) implements TypeKey{
}

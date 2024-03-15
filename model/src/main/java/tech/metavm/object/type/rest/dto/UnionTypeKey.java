package tech.metavm.object.type.rest.dto;

import java.util.Set;

public record UnionTypeKey(Set<String> memberIds) implements TypeKey {
}

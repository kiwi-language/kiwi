package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

public record ArrayTypeKey(int kind, RefDTO elementTypeRef) implements TypeKey{
}

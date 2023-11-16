package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

public record ArrayTypeKey(int kind, RefDTO elementTypeRef) implements TypeKey{
}

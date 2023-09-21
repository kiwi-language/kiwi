package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

public record ArrayTypeParamDTO (
        RefDTO elementTypeRef,
        TypeDTO elementType
) {
}

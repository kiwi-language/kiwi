package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record GetParameterizedTypeRequest(
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<TypeDTO> contextTypes
) {
}

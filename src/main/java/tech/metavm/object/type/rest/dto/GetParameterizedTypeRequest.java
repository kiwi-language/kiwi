package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public record GetParameterizedTypeRequest(
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<TypeDTO> contextTypes
) {
}

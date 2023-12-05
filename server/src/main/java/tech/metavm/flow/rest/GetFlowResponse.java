package tech.metavm.flow.rest;

import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public record GetFlowResponse(
        FlowDTO flow,
        List<TypeDTO> referredTypes
) {
}

package org.metavm.flow.rest;

import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public record GetFlowResponse(
        FlowDTO flow,
        List<TypeDTO> referredTypes
) {
}

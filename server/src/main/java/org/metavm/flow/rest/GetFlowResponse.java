package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public record GetFlowResponse(
        FlowDTO flow,
        List<KlassDTO> referredTypes
) {
}

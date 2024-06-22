package org.metavm.object.type.rest.dto;

import java.util.List;

public record GetTypeResponse(
        KlassDTO type,
        List<KlassDTO> contextTypes
) {
}

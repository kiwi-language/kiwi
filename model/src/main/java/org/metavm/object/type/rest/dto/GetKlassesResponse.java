package org.metavm.object.type.rest.dto;

import java.util.List;

public record GetKlassesResponse(
        List<KlassDTO> types,
        List<KlassDTO> contextTypes
) {
}

package tech.metavm.object.type.rest.dto;

import java.util.List;

public record GetKlassesResponse(
        List<TypeDTO> types,
        List<TypeDTO> contextTypes
) {
}

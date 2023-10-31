package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record GetTypesResponse(
        List<TypeDTO> types,
        List<TypeDTO> contextTypes
) {
}

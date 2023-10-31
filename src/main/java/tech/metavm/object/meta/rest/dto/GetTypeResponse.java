package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record GetTypeResponse(
        TypeDTO type,
        List<TypeDTO> contextTypes
) {
}

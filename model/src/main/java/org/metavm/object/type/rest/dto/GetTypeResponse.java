package org.metavm.object.type.rest.dto;

import java.util.List;

public record GetTypeResponse(
        TypeDTO type,
        List<TypeDTO> contextTypes
) {
}

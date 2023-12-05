package tech.metavm.object.type.rest.dto;

import java.util.List;

public record GetFieldResponse(
        FieldDTO field,
        List<TypeDTO> contextTypes
) {
}

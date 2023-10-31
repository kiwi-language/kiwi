package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record GetFieldResponse(
        FieldDTO field,
        List<TypeDTO> contextTypes
) {
}

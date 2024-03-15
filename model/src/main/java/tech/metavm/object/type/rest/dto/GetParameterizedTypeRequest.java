package tech.metavm.object.type.rest.dto;

import java.util.List;

public record GetParameterizedTypeRequest(
        String templateId,
        List<String> typeArgumentIds,
        List<TypeDTO> contextTypes
) {
}

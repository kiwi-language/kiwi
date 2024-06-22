package org.metavm.object.type.rest.dto;

import java.util.List;

public record GetParameterizedTypeRequest(
        String templateId,
        List<String> typeArgumentIds,
        List<KlassDTO> contextTypes
) {

    public static GetParameterizedTypeRequest create(String templateId, List<String> typeArgumentIds) {
        return new GetParameterizedTypeRequest(templateId, typeArgumentIds, List.of());
    }

}

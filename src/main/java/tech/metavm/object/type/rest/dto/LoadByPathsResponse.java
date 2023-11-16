package tech.metavm.object.type.rest.dto;

import java.util.List;
import java.util.Map;

public record LoadByPathsResponse(
        Map<String, Long> path2typeId,
        List<TypeDTO> types
) {
}

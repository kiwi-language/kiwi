package tech.metavm.object.type.rest.dto;

import java.util.List;

public record BatchSaveRequest(
        List<TypeDTO> types
) {
}

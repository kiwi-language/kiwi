package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record TableDTO(
        Long id,
        String name,
        String desc,
        boolean ephemeral,
        boolean anonymous,
        TitleFieldDTO titleField,
        List<ColumnDTO> fields
) {
}

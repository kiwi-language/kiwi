package tech.metavm.object.type.rest.dto;

import java.util.List;

public record TableDTO(
        Long id,
        String name,
        String code,
        String desc,
        boolean ephemeral,
        boolean anonymous,
        TitleFieldDTO titleField,
        List<ColumnDTO> fields
) {
}

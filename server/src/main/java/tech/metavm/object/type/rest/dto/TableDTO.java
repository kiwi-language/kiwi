package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;

import java.util.List;

public record TableDTO(
        String id,
        String name,
        String code,
        String desc,
        boolean ephemeral,
        boolean anonymous,
        TitleFieldDTO titleField,
        List<ColumnDTO> fields
) implements BaseDTO {
}

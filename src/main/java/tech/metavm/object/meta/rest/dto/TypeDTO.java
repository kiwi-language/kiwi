package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record TypeDTO(
        Long id,
        String name,
        int type,
        boolean ephemeral,
        Long baseTypeId,
        String desc,
        TitleFieldDTO titleField,
        List<FieldDTO> fields
) {

}

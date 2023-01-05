package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record ClassParamDTO(
        Long superTypeId,
        TypeDTO superType,
        List<FieldDTO> fields,
        List<ConstraintDTO> constraints,
        String desc,
        Object extra
) {
}

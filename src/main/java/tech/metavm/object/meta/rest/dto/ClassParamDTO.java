package tech.metavm.object.meta.rest.dto;

import tech.metavm.flow.rest.FlowDTO;

import java.util.List;

public record ClassParamDTO(
        Long superTypeId,
        TypeDTO superType,
        List<FieldDTO> fields,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        String desc,
        Object extra
) {
}

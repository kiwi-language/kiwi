package tech.metavm.object.meta.rest.dto;

import tech.metavm.object.instance.rest.ConditionDTO;

import java.util.List;

public record TypeDTO(
        Long id,
        String name,
        int category,
        boolean ephemeral,
        boolean anonymous,
//        Long elementTypeId,
//        Long underlyingTypeId,
//        TypeDTO elementType,
//        TypeDTO underlyingType,
        TypeDTO rawType,
        List<TypeDTO> typeArguments,
        String desc,
        List<FieldDTO> fields,
        List<ConstraintDTO> constraints,
        List<EnumConstantDTO> enumConstants
// TODO return flow summaries , List<FlowSummaryDTO> flows
) {



}

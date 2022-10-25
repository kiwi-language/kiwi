package tech.metavm.object.meta.rest.dto;

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
        List<EnumConstantDTO> enumConstants
// TODO return flow summaries , List<FlowSummaryDTO> flows
) {



}

package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record TypeDTO(
        Long id,
        String name,
        int type,
        boolean ephemeral,
        boolean anonymous,
        Long baseTypeId,
        TypeDTO baseType,
        String desc,
        TitleFieldDTO titleField,
        List<FieldDTO> fields,
        List<ChoiceOptionDTO> choiceOptions
// TODO return flow summaries , List<FlowSummaryDTO> flows
) {



}

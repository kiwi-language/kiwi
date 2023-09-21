package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record UpdateStaticParamDTO(
        RefDTO typeRef,
        List<UpdateFieldDTO> fields
) {
}

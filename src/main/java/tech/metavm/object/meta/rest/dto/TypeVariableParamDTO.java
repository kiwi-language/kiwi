package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record TypeVariableParamDTO(
        RefDTO genericDeclarationRef,
        List<RefDTO> boundRefs
) {
}

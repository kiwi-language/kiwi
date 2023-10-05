package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record FunctionTypeParamDTO(
        List<RefDTO> parameterTypeRefs,
        RefDTO returnTypeRef
) {
}

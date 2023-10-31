package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;

public record ParameterDTO(
        Long tmpId,
        Long id,
        String name,
        String code,
        RefDTO typeRef
) implements BaseDTO {
}

package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;

import javax.annotation.Nullable;

public record ParameterDTO(
        Long tmpId,
        Long id,
        String name,
        String code,
        RefDTO typeRef,
        ValueDTO condition,
        @Nullable RefDTO templateRef,
        RefDTO callableRef
) implements BaseDTO {
}

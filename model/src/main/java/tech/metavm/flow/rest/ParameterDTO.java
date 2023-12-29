package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;

public record ParameterDTO(
        Long id,
        Long tmpId,
        String name,
        String code,
        RefDTO typeRef,
        @Nullable ValueDTO condition,
        @Nullable RefDTO templateRef,
        RefDTO callableRef
) implements BaseDTO {
}

package tech.metavm.object.view.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;

public record FieldMappingDTO(
        Long id,
        Long tmpId,
        String name,
        @Nullable String code,
        RefDTO typeRef,
        boolean isChild,
        boolean readonly,
        @Nullable RefDTO sourceFieldRef,
        RefDTO targetFieldRef,
        @Nullable RefDTO nestedMappingRef,
        FieldMappingParam param
) implements BaseDTO {
}

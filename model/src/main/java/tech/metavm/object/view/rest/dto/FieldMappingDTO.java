package tech.metavm.object.view.rest.dto;

import tech.metavm.common.BaseDTO;

import javax.annotation.Nullable;

public record FieldMappingDTO(
        String id,
        String name,
        @Nullable String code,
        String typeId,
        boolean isChild,
        boolean readonly,
        @Nullable String sourceFieldId,
        String targetFieldId,
        @Nullable String nestedMappingId,
        FieldMappingParam param
) implements BaseDTO {
}

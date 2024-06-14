package org.metavm.object.view.rest.dto;

import org.metavm.common.BaseDTO;
import org.metavm.object.type.rest.dto.FieldRefDTO;

import javax.annotation.Nullable;

public record FieldMappingDTO(
        String id,
        String name,
        @Nullable String code,
        String type,
        boolean isChild,
        boolean readonly,
        @Nullable String sourceFieldId,
        FieldRefDTO targetFieldRef,
        @Nullable String nestedMappingId,
        FieldMappingParam param
) implements BaseDTO {
}

package org.metavm.object.view.rest.dto;

import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.type.rest.dto.FieldRefDTO;

import javax.annotation.Nullable;

public record FieldMappingDTO(
        String id,
        String name,
        String type,
        boolean isChild,
        boolean readonly,
        @Nullable FieldRefDTO sourceFieldRef,
        FieldRefDTO targetFieldRef,
        @Nullable String nestedMappingId,
        FieldMappingParam param
) implements BaseDTO {
}

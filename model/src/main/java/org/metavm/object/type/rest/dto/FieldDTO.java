package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.instance.rest.FieldValue;

import javax.annotation.Nullable;

public record FieldDTO(
        String id,
        String name,
        String code,
        int access,
        FieldValue defaultValue,
        boolean unique,
        String declaringTypeId,
        @NotNull String type,
        boolean isChild,
        boolean isStatic,
        boolean readonly,
        boolean isTransient,
        boolean lazy,
        @Nullable Integer sourceCodeTag,
        int state
) implements BaseDTO {

}

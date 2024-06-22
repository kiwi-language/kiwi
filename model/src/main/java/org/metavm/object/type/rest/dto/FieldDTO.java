package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceDTO;

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
        boolean lazy,
        @Nullable InstanceDTO staticValue,
        int state
) implements BaseDTO {

}

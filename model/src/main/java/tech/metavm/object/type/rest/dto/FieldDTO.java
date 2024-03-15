package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;

import javax.annotation.Nullable;

public record FieldDTO(
        String id,
        String name,
        String code,
        int access,
        FieldValue defaultValue,
        boolean unique,
        String declaringTypeId,
        String typeId,
        boolean isChild,
        boolean isStatic,
        boolean readonly,
        boolean lazy,
        @Nullable InstanceDTO staticValue,
        int state
) implements BaseDTO {

}

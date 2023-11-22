package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;

import javax.annotation.Nullable;

public record FieldDTO(
        Long tmpId,
        Long id,
        String name,
        String code,
        int access,
        FieldValue defaultValue,
        boolean unique,
        boolean asTitle,
        Long declaringTypeId,
        RefDTO typeRef,
        boolean isChild,
        boolean isStatic,
        boolean lazy,
        @Nullable InstanceDTO staticValue,
        int state
) implements BaseDTO {

    public Long typeId() {
        return typeRef.id();
    }

    public static FieldDTO create(String name, long typeId) {
        return create(null, name, typeId);
    }

    public static FieldDTO create(Long id, String name, long typeId) {
        return create(id, name, null, typeId);
    }


    public static FieldDTO create(Long id, String name, Long declaringTypeId, long typeId) {
        return FieldDTOBuilder.newBuilder(name, null, RefDTO.fromId(typeId))
                .id(id)
                .declaringTypeId(declaringTypeId)
                .build();
    }

}

package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Access;

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
        boolean isStatic
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
        return new FieldDTO(
                null, id, name, null,  Access.GLOBAL.code(),
                null, false, false, declaringTypeId,
                RefDTO.ofId(typeId), false, false
        );
    }

    public static FieldDTO createSimple(Long id,
                                        String name,
                                        int access,
                                        FieldValue defaultValue,
                                        Long declaringTypeId,
                                        long typeId) {
        return new FieldDTO(
                null,
                id,
                name,
                null,
                access,
                defaultValue,
                false,
                false,
                declaringTypeId,
                RefDTO.ofId(typeId),
                false,
                false
        );
    }

}

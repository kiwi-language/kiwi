package tech.metavm.object.meta.rest.dto;

import tech.metavm.object.meta.Access;

public record FieldDTO(
        Long id,
        String name,
        int access,
        Object defaultValue,
        boolean unique,
        boolean asTitle,
        Long declaringTypeId,
        Long typeId,
        TypeDTO type,
        boolean isChild
) {

    public static FieldDTO create(String name, long typeId) {
        return create(null, name, typeId);
    }

    public static FieldDTO create(Long id, String name, long typeId) {
        return create(id, name, null, typeId);
    }


    public static FieldDTO create(Long id, String name, Long declaringTypeId, long typeId) {
        return new FieldDTO(
                id, name, Access.GLOBAL.code(),
                null, false, false, declaringTypeId,
                typeId, null, false
        );
    }

    public static FieldDTO createSimple(long id,
                                        String name,
                                        int access,
                                        Object defaultValue,
                                        long declaringTypeId,
                                        long typeId) {
        return new FieldDTO(
                id,
                name,
                access,
                defaultValue,
                false,
                false,
                declaringTypeId,
                typeId,
                null,
                false
        );
    }

}

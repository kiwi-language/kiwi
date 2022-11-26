package tech.metavm.object.meta.rest.dto;

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

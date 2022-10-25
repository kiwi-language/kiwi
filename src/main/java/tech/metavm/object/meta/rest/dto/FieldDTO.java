package tech.metavm.object.meta.rest.dto;

public record FieldDTO(
        Long id,
        String name,
        int access,
        Object defaultValue,
        boolean unique,
        boolean asTitle,
        long declaringTypeId,
        Long typeId,
        TypeDTO type
) {

}

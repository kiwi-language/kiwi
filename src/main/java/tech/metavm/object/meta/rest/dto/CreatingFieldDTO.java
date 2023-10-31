package tech.metavm.object.meta.rest.dto;

public record CreatingFieldDTO(
        String name,
        String code,
        long typeId,
        String typeName,
        boolean unique
) {
}

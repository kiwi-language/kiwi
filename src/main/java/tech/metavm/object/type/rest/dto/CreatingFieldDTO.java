package tech.metavm.object.type.rest.dto;

public record CreatingFieldDTO(
        String name,
        String code,
        long typeId,
        String typeName,
        boolean unique
) {
}

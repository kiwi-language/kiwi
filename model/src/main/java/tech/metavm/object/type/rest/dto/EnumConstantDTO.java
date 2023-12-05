package tech.metavm.object.type.rest.dto;

public record EnumConstantDTO (
        Long id,
        Long ownerId,
        int ordinal,
        String name
) {
}

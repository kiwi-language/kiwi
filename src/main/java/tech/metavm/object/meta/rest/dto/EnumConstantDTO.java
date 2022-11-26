package tech.metavm.object.meta.rest.dto;

public record EnumConstantDTO (
        Long id,
        Long ownerId,
        int ordinal,
        String name
) {
}

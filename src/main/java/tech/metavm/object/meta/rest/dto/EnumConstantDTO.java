package tech.metavm.object.meta.rest.dto;

public record EnumConstantDTO (
        Long id,
        long ownerId,
        int ordinal,
        String name
) {
}

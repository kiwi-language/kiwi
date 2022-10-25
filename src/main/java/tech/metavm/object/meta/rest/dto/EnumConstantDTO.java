package tech.metavm.object.meta.rest.dto;

public record EnumConstantDTO (
        long id,
        long ownerId,
        int ordinal,
        String name
) {
}

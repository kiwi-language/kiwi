package org.metavm.object.type.rest.dto;

public record EnumConstantDTO (
        String id,
        String ownerId,
        int ordinal,
        String name
) {
}

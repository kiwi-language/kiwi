package org.metavm.object.type.rest.dto;

public record CreatingFieldDTO(
        String name,
        String typeId,
        String typeName,
        boolean unique
) {
}

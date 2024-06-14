package org.metavm.object.type.rest.dto;

public record TitleFieldDTO (
        Long tmpId,
        String name,
        int type,
        boolean unique,
        Object defaultValue
) {
}

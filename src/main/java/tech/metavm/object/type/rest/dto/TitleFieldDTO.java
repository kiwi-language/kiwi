package tech.metavm.object.type.rest.dto;

public record TitleFieldDTO (
        String name,
        int type,
        boolean unique,
        Object defaultValue
) {
}

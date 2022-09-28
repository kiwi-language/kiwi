package tech.metavm.object.meta.rest.dto;

public record TitleFieldDTO (
        String name,
        int type,
        boolean unique,
        Object defaultValue
) {
}

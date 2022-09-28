package tech.metavm.object.meta.rest.dto;

public record ChoiceOptionDTO(
        Long id,
        String name,
        int order,
        boolean defaultSelected
) {
}

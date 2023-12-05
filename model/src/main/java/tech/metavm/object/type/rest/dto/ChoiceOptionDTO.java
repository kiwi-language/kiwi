package tech.metavm.object.type.rest.dto;

public record ChoiceOptionDTO(
        Long id,
        String name,
        int order,
        boolean defaultSelected
) {

}

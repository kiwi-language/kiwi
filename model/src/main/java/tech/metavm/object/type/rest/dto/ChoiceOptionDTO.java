package tech.metavm.object.type.rest.dto;

public record ChoiceOptionDTO(
        String id,
        String name,
        int order,
        boolean defaultSelected
) {

}

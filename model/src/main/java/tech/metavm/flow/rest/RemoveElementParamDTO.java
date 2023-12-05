package tech.metavm.flow.rest;

public record RemoveElementParamDTO(
        ValueDTO array,
        ValueDTO element
) {
}

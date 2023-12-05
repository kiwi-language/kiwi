package tech.metavm.flow.rest;

public record SetElementParamDTO(
        ValueDTO array,
        ValueDTO index,
        ValueDTO element
) {
}

package tech.metavm.flow.rest;

public record RemoveElementAtParamDTO(
        ValueDTO array,
        ValueDTO index
) {
}

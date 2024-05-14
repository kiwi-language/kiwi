package tech.metavm.flow.rest;

public record ArgumentDTO(
        Long tmpId,
        ParameterRefDTO parameterRef,
        ValueDTO value
) {
}

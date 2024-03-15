package tech.metavm.flow.rest;

public record ArgumentDTO(
        Long tmpId,
        String parameterId,
        ValueDTO value
) {
}

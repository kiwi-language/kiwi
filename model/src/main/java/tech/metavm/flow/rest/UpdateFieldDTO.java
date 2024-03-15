package tech.metavm.flow.rest;

public record UpdateFieldDTO(
        String fieldId,
        int opCode,
        ValueDTO value
) {
}

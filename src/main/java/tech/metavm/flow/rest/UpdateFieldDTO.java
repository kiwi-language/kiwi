package tech.metavm.flow.rest;

public record UpdateFieldDTO(
        Long fieldId,
        int opCode,
        ValueDTO value
) {
}

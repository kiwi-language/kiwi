package tech.metavm.flow.rest;

public record UpdateFieldDTO(
        long fieldId,
        int opCode,
        ValueDTO value
) {
}

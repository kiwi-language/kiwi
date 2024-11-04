package org.metavm.flow.rest;

public record NodeValueDTO(String nodeId) implements ValueDTO {
    @Override
    public int getKind() {
        return ValueKindCodes.NODE;
    }
}

package org.metavm.flow.rest;

public record TypeValueDTO(String type) implements ValueDTO {
    @Override
    public int getKind() {
        return ValueKindCodes.TYPE;
    }
}

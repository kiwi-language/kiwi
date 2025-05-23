package org.metavm.object.instance.rest.dto;

public record BoolValueDTO(boolean value) implements ValueDTO {
    @Override
    public String getKind() {
        return "bool";
    }
}

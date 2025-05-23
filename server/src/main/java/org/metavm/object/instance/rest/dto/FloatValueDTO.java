package org.metavm.object.instance.rest.dto;

public record FloatValueDTO(double value) implements ValueDTO {
    @Override
    public String getKind() {
        return "float";
    }
}

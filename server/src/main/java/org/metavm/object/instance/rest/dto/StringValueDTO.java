package org.metavm.object.instance.rest.dto;

public record StringValueDTO(String value) implements ValueDTO {
    @Override
    public String getKind() {
        return "string";
    }
}

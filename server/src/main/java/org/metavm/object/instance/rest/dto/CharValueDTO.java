package org.metavm.object.instance.rest.dto;

public record CharValueDTO(char value) implements ValueDTO {
    @Override
    public String getKind() {
        return "char";
    }
}

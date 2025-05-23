package org.metavm.object.instance.rest.dto;

public record IntValueDTO(long value) implements ValueDTO {
    @Override
    public String getKind() {
        return "integer";
    }
}

package org.metavm.api.dto;

public record PrimitiveTypeDTO(String name) implements TypeDTO {
    @Override
    public String getKind() {
        return "primitive";
    }
}

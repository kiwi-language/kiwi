package org.metavm.api.dto;

public record ArrayTypeDTO(TypeDTO elementType) implements TypeDTO {
    @Override
    public String getKind() {
        return "array";
    }
}

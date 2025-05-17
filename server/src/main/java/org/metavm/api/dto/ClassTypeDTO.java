package org.metavm.api.dto;

public record ClassTypeDTO(String qualifiedName) implements TypeDTO {
    @Override
    public String getKind() {
        return "class";
    }
}

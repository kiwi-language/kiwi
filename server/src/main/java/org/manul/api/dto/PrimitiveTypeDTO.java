package org.manul.api.dto;

import org.jsonk.Json;

@Json
public record PrimitiveTypeDTO(String name) implements TypeDTO {
    @Override
    public String getKind() {
        return "primitive";
    }
}

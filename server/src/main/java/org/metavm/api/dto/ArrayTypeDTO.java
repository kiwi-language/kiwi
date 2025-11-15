package org.metavm.api.dto;

import org.jsonk.Json;

@Json
public record ArrayTypeDTO(TypeDTO elementType) implements TypeDTO {
    @Override
    public String getKind() {
        return "array";
    }
}

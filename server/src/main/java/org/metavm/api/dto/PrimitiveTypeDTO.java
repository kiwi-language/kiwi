package org.metavm.api.dto;

import org.jsonk.Json;

@Json
public record PrimitiveTypeDTO(String name) implements TypeDTO {
    @Override
    public String getKind() {
        return "primitive";
    }
}

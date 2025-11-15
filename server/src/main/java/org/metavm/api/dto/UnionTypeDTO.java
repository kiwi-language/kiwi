package org.metavm.api.dto;

import org.jsonk.Json;

import java.util.List;

@Json
public record UnionTypeDTO(List<TypeDTO> alternatives) implements TypeDTO {
    @Override
    public String getKind() {
        return "union";
    }
}

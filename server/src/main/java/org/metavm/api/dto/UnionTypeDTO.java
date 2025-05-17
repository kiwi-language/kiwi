package org.metavm.api.dto;

import java.util.List;

public record UnionTypeDTO(List<TypeDTO> alternatives) implements TypeDTO {
    @Override
    public String getKind() {
        return "union";
    }
}

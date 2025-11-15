package org.metavm.api.dto;

import org.jsonk.Json;

@Json
public record ClassTypeDTO(String qualifiedName) implements TypeDTO {
    @Override
    public String getKind() {
        return "class";
    }
}

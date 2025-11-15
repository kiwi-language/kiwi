package org.metavm.api.dto;

import org.jsonk.Json;
import org.jsonk.JsonProperty;

import java.util.List;

@Json
public record MethodDTO(
        String access,
        @JsonProperty("abstract")
        boolean isAbstract,
        String name,
        List<ParameterDTO> parameters,
        TypeDTO returnType,
        String label
) {
}

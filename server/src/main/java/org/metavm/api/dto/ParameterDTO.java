package org.metavm.api.dto;

import org.jsonk.Json;

@Json
public record ParameterDTO(
        String name,
        TypeDTO type,
        String label
) {
}

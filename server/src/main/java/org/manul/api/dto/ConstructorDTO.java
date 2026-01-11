package org.manul.api.dto;

import org.jsonk.Json;

import java.util.List;

@Json
public record ConstructorDTO(
        List<ParameterDTO> parameters
) {
}

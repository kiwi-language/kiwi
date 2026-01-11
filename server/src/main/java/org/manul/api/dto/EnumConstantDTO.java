package org.manul.api.dto;

import org.jsonk.Json;

@Json
public record EnumConstantDTO(
        String name,
        String label
) {
}

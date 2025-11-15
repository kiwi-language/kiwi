package org.metavm.api.dto;

import org.jsonk.Json;

@Json
public record EnumConstantDTO(
        String name,
        String label
) {
}

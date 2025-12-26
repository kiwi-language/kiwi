package org.metavm.application.rest.dto;

import org.jsonk.Json;

@Json
public record ApplicationDTO(
        Long id,
        String name,
        String ownerId
) {
}

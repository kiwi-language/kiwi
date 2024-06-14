package org.metavm.application.rest.dto;

public record ApplicationDTO(
        Long id,
        String name,
        String ownerId
) {
}

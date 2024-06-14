package org.metavm.object.view.rest.dto;

public record ObjectMappingRefDTO(
        String declaringType,
        String rawMappingId
) {
}

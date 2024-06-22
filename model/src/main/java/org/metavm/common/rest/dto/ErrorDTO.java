package org.metavm.common.rest.dto;

public record ErrorDTO(
        int elementKind,
        String elementId,
        String message
) {
}

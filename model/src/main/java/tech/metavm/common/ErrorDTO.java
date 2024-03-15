package tech.metavm.common;

public record ErrorDTO(
        int elementKind,
        String elementId,
        String message
) {
}

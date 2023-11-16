package tech.metavm.common;

public record ErrorDTO(
        int elementKind,
        RefDTO elementRef,
        String message
) {
}

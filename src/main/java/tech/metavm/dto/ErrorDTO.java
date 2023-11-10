package tech.metavm.dto;

public record ErrorDTO(
        int elementKind,
        RefDTO elementRef,
        String message
) {
}

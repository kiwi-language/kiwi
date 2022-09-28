package tech.metavm.entity;

public record EntityId (
        Class<?> type,
        Long id
) {
}

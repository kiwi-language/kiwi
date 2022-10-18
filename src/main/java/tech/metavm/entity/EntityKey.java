package tech.metavm.entity;

public record EntityKey(
        Class<?> type,
        long id
) {
}

package tech.metavm.entity;

public record EntityIndexQueryItem(
        String fieldName,
        IndexOperator operator,
        Object value
) {
}

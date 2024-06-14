package org.metavm.entity;

public record EntityIndexQueryItem(
        String fieldName,
        IndexOperator operator,
        Object value
) {
}

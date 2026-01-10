package org.manul.entity;

public record EntityIndexQueryItem(
        String fieldName,
        IndexOperator operator,
        Object value
) {
}

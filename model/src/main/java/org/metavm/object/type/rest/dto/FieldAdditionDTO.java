package org.metavm.object.type.rest.dto;

public record FieldAdditionDTO(
        String klassId,
        String fieldName,
        int fieldTag
) {
}

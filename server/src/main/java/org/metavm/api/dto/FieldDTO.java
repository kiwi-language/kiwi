package org.metavm.api.dto;

public record FieldDTO(
    String access,
    String name,
    TypeDTO type
) {
}

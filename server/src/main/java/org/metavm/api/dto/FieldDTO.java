package org.metavm.api.dto;

import org.jsonk.Json;

import javax.annotation.Nullable;

@Json
public record FieldDTO(
    String access,
    String name,
    TypeDTO type,
    boolean summary,
    String label,
    @Nullable String numberFormat
) {
}

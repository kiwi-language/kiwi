package org.metavm.api.dto;

import javax.annotation.Nullable;

public record FieldDTO(
    String access,
    String name,
    TypeDTO type,
    boolean summary,
    String label,
    @Nullable String numberFormat
) {
}

package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.ValueDTO;

import javax.annotation.Nullable;

public record IndexFieldDTO(
        Long id,
        String name,
        @Nullable String code,
        ValueDTO value
) {
}

package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.flow.rest.ValueDTO;

import javax.annotation.Nullable;

public record IndexFieldDTO(
        String id,
        String name,
        @Nullable String code,
        ValueDTO value
) implements BaseDTO {
}

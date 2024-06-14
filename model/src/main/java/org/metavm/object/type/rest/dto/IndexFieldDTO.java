package org.metavm.object.type.rest.dto;

import org.metavm.common.BaseDTO;
import org.metavm.flow.rest.ValueDTO;

import javax.annotation.Nullable;

public record IndexFieldDTO(
        String id,
        String name,
        @Nullable String code,
        ValueDTO value
) implements BaseDTO {
}

package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.type.rest.dto.FieldRefDTO;

public record FieldParamDTO (
        String id,
        FieldRefDTO fieldRef,
        ValueDTO value
) implements BaseDTO {

    public static FieldParamDTO create(FieldRefDTO fieldRef, ValueDTO value) {
        return new FieldParamDTO(null, fieldRef, value);
    }

}

package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;
import tech.metavm.object.type.rest.dto.FieldRefDTO;

public record FieldParamDTO (
        String id,
        FieldRefDTO fieldRef,
        ValueDTO value
) implements BaseDTO {

    public static FieldParamDTO create(FieldRefDTO fieldRef, ValueDTO value) {
        return new FieldParamDTO(null, fieldRef, value);
    }

}

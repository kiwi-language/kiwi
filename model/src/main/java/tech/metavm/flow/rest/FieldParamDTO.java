package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.util.NncUtils;

public record FieldParamDTO (
        String id,
        String fieldId,
        ValueDTO value
) implements BaseDTO {

    public static FieldParamDTO create(String fieldId, ValueDTO value) {
        return new FieldParamDTO(null, fieldId, value);
    }

}

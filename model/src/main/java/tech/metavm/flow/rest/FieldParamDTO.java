package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.util.NncUtils;

public record FieldParamDTO (
        Long id,
        Long tmpId,
        RefDTO fieldRef,
        ValueDTO value
) implements BaseDTO {

    public static FieldParamDTO create(RefDTO fieldRef, ValueDTO value) {
        return new FieldParamDTO(null, NncUtils.randomNonNegative(), fieldRef, value);
    }

}

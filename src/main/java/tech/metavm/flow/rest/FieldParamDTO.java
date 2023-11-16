package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;

public record FieldParamDTO (
        Long id,
        Long tmpId,
        RefDTO fieldRef,
        ValueDTO value
) implements BaseDTO {

}

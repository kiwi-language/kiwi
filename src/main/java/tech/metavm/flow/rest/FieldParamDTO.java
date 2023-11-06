package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;

public record FieldParamDTO (
        Long id,
        Long tmpId,
        RefDTO fieldRef,
        ValueDTO value
) implements BaseDTO {

}

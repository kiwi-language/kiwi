package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record FieldParamDTO (
        RefDTO fieldRef,
        ValueDTO value
) {

}

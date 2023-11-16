package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record ArgumentDTO(
        Long tmpId,
        RefDTO parameterRef,
        ValueDTO value
) {
}

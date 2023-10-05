package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record ArgumentDTO(
        Long tmpId,
        RefDTO parameterRef,
        ValueDTO value
) {
}

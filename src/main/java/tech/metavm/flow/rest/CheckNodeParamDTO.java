package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record CheckNodeParamDTO(
        ValueDTO condition,
        RefDTO exitRef
) {
}

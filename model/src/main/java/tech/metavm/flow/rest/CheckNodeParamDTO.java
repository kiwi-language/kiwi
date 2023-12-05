package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record CheckNodeParamDTO(
        ValueDTO condition,
        RefDTO exitRef
) {
}

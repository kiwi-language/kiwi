package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record CheckNodeParam(
        ValueDTO condition,
        RefDTO exitRef
) {
}

package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record UnmapNodeParam(
        ValueDTO view,
        RefDTO mappingRef
) {
}

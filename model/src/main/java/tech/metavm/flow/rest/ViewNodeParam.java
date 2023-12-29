package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record ViewNodeParam(
        ValueDTO source,
        RefDTO mappingRef
) {
}

package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record MapNodeParam(
        ValueDTO source,
        RefDTO mappingRef
) {
}

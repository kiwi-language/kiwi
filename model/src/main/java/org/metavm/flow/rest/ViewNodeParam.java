package org.metavm.flow.rest;

import org.metavm.common.RefDTO;

public record ViewNodeParam(
        ValueDTO source,
        RefDTO mappingRef
) {
}

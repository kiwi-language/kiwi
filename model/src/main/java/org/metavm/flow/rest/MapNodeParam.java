package org.metavm.flow.rest;

import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;

public record MapNodeParam(
        ValueDTO source,
        ObjectMappingRefDTO mappingRef
) {
}

package tech.metavm.flow.rest;

import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;

public record MapNodeParam(
        ValueDTO source,
        ObjectMappingRefDTO mappingRef
) {
}

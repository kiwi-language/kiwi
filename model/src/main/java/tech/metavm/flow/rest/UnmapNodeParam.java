package tech.metavm.flow.rest;

import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;

public record UnmapNodeParam(
        ValueDTO view,
        ObjectMappingRefDTO mappingRef
) {
}

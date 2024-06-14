package org.metavm.flow.rest;

import java.util.List;

public record UpdateObjectNodeParam(
        ValueDTO objectId,
        List<UpdateFieldDTO> fields
) {
}

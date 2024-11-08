package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldRefDTO;

public record SetFieldNodeParam(
        ValueDTO objectId,
        FieldRefDTO fieldRef,
        ValueDTO value
) {
}

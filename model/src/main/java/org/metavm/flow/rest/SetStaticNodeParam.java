package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldRefDTO;

public record SetStaticNodeParam(
        FieldRefDTO fieldRef,
        ValueDTO value
) {
}

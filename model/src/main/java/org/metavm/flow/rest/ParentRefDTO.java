package org.metavm.flow.rest;

import org.metavm.object.type.rest.dto.FieldRefDTO;

import javax.annotation.Nullable;

public record ParentRefDTO(
        ValueDTO parent,
        @Nullable FieldRefDTO fieldRef
) {
}

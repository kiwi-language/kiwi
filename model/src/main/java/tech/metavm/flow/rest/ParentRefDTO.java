package tech.metavm.flow.rest;

import tech.metavm.object.type.rest.dto.FieldRefDTO;

import javax.annotation.Nullable;

public record ParentRefDTO(
        ValueDTO parent,
        @Nullable FieldRefDTO fieldRef
) {
}

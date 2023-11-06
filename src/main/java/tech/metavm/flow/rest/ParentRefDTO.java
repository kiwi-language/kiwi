package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;

public record ParentRefDTO(
        ValueDTO parent,
        @Nullable RefDTO fieldRef
) {
}

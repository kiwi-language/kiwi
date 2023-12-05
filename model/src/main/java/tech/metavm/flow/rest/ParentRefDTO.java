package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;

public record ParentRefDTO(
        ValueDTO parent,
        @Nullable RefDTO fieldRef
) {
}

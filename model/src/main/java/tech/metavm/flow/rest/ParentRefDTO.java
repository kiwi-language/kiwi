package tech.metavm.flow.rest;

import javax.annotation.Nullable;

public record ParentRefDTO(
        ValueDTO parent,
        @Nullable String fieldId
) {
}

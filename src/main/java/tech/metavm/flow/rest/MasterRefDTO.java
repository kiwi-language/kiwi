package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;

public record MasterRefDTO(
        ValueDTO master,
        @Nullable RefDTO fieldRef
) {
}

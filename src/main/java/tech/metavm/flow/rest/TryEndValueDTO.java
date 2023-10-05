package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record TryEndValueDTO(
        RefDTO raiseNodeRef,
        ValueDTO value
) {
}

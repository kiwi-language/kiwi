package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record TryEndValueDTO(
        RefDTO raiseNodeRef,
        ValueDTO value
) {
}

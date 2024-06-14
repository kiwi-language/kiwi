package org.metavm.flow.rest;

public record TryEndValueDTO(
        String raiseNodeId,
        ValueDTO value
) {
}

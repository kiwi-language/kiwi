package org.metavm.flow.rest;

public record JoinedValueDTO(
        String sourceNodeId,
        ValueDTO value
) {
}

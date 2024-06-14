package org.metavm.flow.rest;

public record CheckNodeParam(
        ValueDTO condition,
        String exitId
) {
}

package org.metavm.flow.rest;

public record IfNodeParam(
        ValueDTO condition,
        String targetId
) {
}

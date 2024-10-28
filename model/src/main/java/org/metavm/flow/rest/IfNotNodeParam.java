package org.metavm.flow.rest;

public record IfNotNodeParam(
        ValueDTO condition,
        String targetId
) {
}

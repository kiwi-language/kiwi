package org.metavm.flow.rest;

public record SetElementNodeParam(
        ValueDTO array,
        ValueDTO index,
        ValueDTO element
) {
}

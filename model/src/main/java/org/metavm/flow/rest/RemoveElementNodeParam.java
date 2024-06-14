package org.metavm.flow.rest;

public record RemoveElementNodeParam(
        ValueDTO array,
        ValueDTO element
) {
}

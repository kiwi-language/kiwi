package org.metavm.flow.rest;

public record LoopParam(
        ValueDTO condition,
        Long firstChildId
) {

}

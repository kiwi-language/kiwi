package org.metavm.flow.rest;

public record ConditionalValueDTO(
        String branchId,
        ValueDTO value
) {
}

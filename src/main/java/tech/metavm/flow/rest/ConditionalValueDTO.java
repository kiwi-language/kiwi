package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record ConditionalValueDTO(
        RefDTO branchRef,
        ValueDTO value
) {
}

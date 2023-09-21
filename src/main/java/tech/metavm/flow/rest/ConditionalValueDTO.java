package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record ConditionalValueDTO(
        RefDTO branchRef,
        ValueDTO value
) {
}

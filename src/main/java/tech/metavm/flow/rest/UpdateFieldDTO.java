package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

public record UpdateFieldDTO(
        RefDTO fieldRef,
        int opCode,
        ValueDTO value
) {
}

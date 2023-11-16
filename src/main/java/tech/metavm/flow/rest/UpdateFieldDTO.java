package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record UpdateFieldDTO(
        RefDTO fieldRef,
        int opCode,
        ValueDTO value
) {
}

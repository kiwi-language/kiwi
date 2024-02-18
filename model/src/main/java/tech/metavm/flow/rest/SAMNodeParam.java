package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record SAMNodeParam(
        RefDTO samInterfaceRef,
        ValueDTO function
) {
}

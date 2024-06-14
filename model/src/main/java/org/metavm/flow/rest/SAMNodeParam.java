package org.metavm.flow.rest;

import org.metavm.common.RefDTO;

public record SAMNodeParam(
        RefDTO samInterfaceRef,
        ValueDTO function
) {
}

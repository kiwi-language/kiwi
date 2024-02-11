package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record IndexSelectNodeParam(
        RefDTO indexRef,
        IndexQueryKeyDTO key
) {
}

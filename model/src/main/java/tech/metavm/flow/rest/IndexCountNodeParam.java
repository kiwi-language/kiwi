package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record IndexCountNodeParam(
        RefDTO indexRef,
        IndexQueryKeyDTO from,
        IndexQueryKeyDTO to
) {
}

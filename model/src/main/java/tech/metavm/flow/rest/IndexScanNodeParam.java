package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record IndexScanNodeParam(
        RefDTO indexRef,
        IndexQueryKeyDTO from,
        IndexQueryKeyDTO to
) {
}

package org.metavm.flow.rest;

public record IndexScanNodeParam(
        String indexId,
        IndexQueryKeyDTO from,
        IndexQueryKeyDTO to
) {
}

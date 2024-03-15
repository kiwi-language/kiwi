package tech.metavm.flow.rest;

public record IndexScanNodeParam(
        String indexId,
        IndexQueryKeyDTO from,
        IndexQueryKeyDTO to
) {
}

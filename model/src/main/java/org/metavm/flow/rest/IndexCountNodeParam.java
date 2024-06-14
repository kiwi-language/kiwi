package org.metavm.flow.rest;

public record IndexCountNodeParam(
        String indexId,
        IndexQueryKeyDTO from,
        IndexQueryKeyDTO to
) {
}

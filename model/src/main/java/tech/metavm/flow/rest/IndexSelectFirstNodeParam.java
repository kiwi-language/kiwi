package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record IndexSelectFirstNodeParam(
        RefDTO indexRef,
        IndexQueryKeyDTO key
) {
}

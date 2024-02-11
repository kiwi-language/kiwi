package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record IndexQueryKeyItemDTO(
        RefDTO indexFieldRef,
        ValueDTO value
) {
}

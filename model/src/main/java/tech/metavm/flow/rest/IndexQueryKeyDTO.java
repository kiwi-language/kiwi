package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import java.util.List;

public record IndexQueryKeyDTO(
        RefDTO indexRef,
    List<IndexQueryKeyItemDTO> items
) {
}

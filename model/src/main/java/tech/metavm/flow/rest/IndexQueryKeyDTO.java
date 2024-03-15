package tech.metavm.flow.rest;

import java.util.List;

public record IndexQueryKeyDTO(
        String indexId,
        List<IndexQueryKeyItemDTO> items
) {
}

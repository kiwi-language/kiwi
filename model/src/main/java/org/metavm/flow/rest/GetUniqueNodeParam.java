package org.metavm.flow.rest;

import java.util.List;

public record GetUniqueNodeParam(
        String indexId,
        List<ValueDTO> values
) {
}

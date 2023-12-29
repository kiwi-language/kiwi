package tech.metavm.flow.rest;

import java.util.List;

public record GetUniqueNodeParam(
        long indexId,
        List<ValueDTO> values
) {
}

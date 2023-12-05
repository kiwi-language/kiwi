package tech.metavm.flow.rest;

import java.util.List;

public record GetUniqueParamDTO (
        long indexId,
        List<ValueDTO> values
) {
}

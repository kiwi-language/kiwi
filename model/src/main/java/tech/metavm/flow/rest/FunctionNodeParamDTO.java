package tech.metavm.flow.rest;

import java.util.List;

public record FunctionNodeParamDTO(
        ValueDTO func,
        List<ValueDTO> arguments
) {
}

package tech.metavm.flow.rest;

import java.util.List;

public record FunctionNodeParam(
        ValueDTO func,
        List<ValueDTO> arguments
) {
}

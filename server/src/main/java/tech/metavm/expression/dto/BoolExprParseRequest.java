package tech.metavm.expression.dto;

import tech.metavm.flow.rest.ValueDTO;

public record BoolExprParseRequest (
        ValueDTO value,
        ParsingContextDTO context
) {
}

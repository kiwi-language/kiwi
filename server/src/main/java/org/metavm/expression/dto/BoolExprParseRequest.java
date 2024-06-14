package org.metavm.expression.dto;

import org.metavm.flow.rest.ValueDTO;

public record BoolExprParseRequest (
        ValueDTO value,
        ParsingContextDTO context
) {
}

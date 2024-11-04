package org.metavm.flow.rest;


public record ExpressionValueDTO(
        String expression
) implements ValueDTO{
    @Override
    public int getKind() {
        return ValueKindCodes.EXPRESSION;
    }
}

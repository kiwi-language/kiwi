package tech.metavm.transpile.ir;

import tech.metavm.util.InternalException;

public record ArrayElementExpression (
    IRExpression array,
    IRExpression index
) implements IRExpression{

    public ArrayElementExpression {
        if (!(array.type() instanceof IRArrayType)) {
            throw new InternalException(array + " is not an array expression");
        }
    }

    @Override
    public IRType type() {
        return ((IRArrayType) array.type()).getElementType();
    }

}

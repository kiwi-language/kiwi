package tech.metavm.transpile.ir;

import java.util.List;

public record TensorExpression(
        IRType elementType,
        int rank,
        List<IRExpression> dimensions
) implements IRExpression{

    @Override
    public IRType type() {
        var type = elementType;
        for (int i = 0; i < rank; i++) {
            type = type.getArrayType();
        }
        return type;
    }
}

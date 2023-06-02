package tech.metavm.transpile.ir;

import java.util.List;

public record StaticMethodReference(
        IRType declaringType,
        IRMethod method,
        List<IRType> typeArguments) implements IRExpression {

    @Override
    public FunctionType type() {
        return null;
    }
}

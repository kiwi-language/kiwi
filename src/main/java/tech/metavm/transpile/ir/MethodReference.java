package tech.metavm.transpile.ir;

import java.util.List;

public record MethodReference(
        IRExpression instance,
        IRMethod method,
        List<IRType> typeArguments) implements IRExpression {

    @Override
    public IRType type() {
        return null;
    }
}

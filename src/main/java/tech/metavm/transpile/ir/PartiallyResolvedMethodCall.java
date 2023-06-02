package tech.metavm.transpile.ir;

import tech.metavm.transpile.ir.gen2.Graph;

import javax.annotation.Nullable;
import java.util.List;

public record PartiallyResolvedMethodCall(
        @Nullable IRExpression instance,
        IRMethod method,
        List<IRExpression> arguments,
        Graph graph
) implements IRExpression{
    @Override
    public IRType type() {
        return method.returnType();
    }

    public MethodCall toMethodCall(List<IRType> typeArguments, List<IRExpression> resolvedArguments) {
        return new MethodCall(instance, typeArguments, method, resolvedArguments, graph);
    }

}

package tech.metavm.transpile.ir;

import tech.metavm.transpile.ir.gen2.Graph;

import javax.annotation.Nullable;
import java.util.List;

public record UnresolvedMethodCall(
        @Nullable IRExpression instance,
        IRType declaringType,
        String methodeName,
        List<IRExpression> arguments
//        Graph graph
) implements IRExpression {

    @Override
    public IRType type() {
        return null;
    }

    public List<IRMethod> methods() {
        return IRUtil.getRawClass(declaringType).getMethods(methodeName);
    }

    public PartiallyResolvedMethodCall toPartiallyResolved(IRMethod method, List<IRExpression> resolvedArguments, Graph graph) {
        return new PartiallyResolvedMethodCall(instance, method, resolvedArguments, graph);
    }

}

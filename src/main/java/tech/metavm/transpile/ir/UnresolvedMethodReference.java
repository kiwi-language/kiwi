package tech.metavm.transpile.ir;

import java.util.List;

public record UnresolvedMethodReference(
        IRExpression instance,
        String methodName,
        List<IRType> typeArguments
) implements UnresolvedFunctionExpression {

    @Override
    public List<? extends IRFunction> functions() {
        return instance.klass().getMethods(methodName);
    }

    @Override
    public IRExpression resolve(IRType type) {
        return null;
    }
}

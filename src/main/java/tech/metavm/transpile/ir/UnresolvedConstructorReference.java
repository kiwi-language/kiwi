package tech.metavm.transpile.ir;

import java.util.List;

public record UnresolvedConstructorReference(
        IRType declaringType,
        List<IRType> typeArguments
) implements UnresolvedFunctionExpression {

    @Override
    public List<? extends IRFunction> functions() {
        return IRUtil.getRawClass(declaringType).getConstructors();
    }

    @Override
    public IRExpression resolve(IRType type) {
        return null;
    }
}

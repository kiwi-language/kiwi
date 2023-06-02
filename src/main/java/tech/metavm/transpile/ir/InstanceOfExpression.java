package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;

public record InstanceOfExpression(
        IRExpression expression,
        IRType type,
        LocalVariable variable
) implements IRExpression{
    @Override
    public IRType type() {
        return IRTypeUtil.fromClass(boolean.class);
    }
}

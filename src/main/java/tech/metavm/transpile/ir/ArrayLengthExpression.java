package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;

public record ArrayLengthExpression(
        IRExpression array
) implements IRExpression {
    @Override
    public IRType type() {
        return IRTypeUtil.intClass();
    }
}

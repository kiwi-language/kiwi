package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;

import java.util.List;

public record ConditionalExpression(
        IRExpression condition,
        IRExpression first,
        IRExpression second
) implements IRExpression {

    @Override
    public IRType type() {
        return IRTypeUtil.getCompatibleType(List.of(first.type(), second.type()));
    }
}

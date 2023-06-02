package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;

public record ConstantExpression(
        Object value,
        IRType type
) implements IRExpression {

    public static ConstantExpression create(Object value) {
        return new ConstantExpression(
                value,
                value == null ? IRAnyType.getInstance() : IRTypeUtil.fromClass(value.getClass())
        );
    }

}

package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRExpression;
import tech.metavm.transpile.ir.TypeVariable;

public record XTypeKey(
        IRExpression expression,
        TypeVariable<?> typeVariable
) {
}

package tech.metavm.transpile.ir;

import javax.annotation.Nullable;

public record Return(
        @Nullable IRExpression expression
) implements Statement {
}

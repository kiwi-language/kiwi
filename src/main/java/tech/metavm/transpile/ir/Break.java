package tech.metavm.transpile.ir;

import javax.annotation.Nullable;

public record Break(
        @Nullable Label label
) implements Statement {
}

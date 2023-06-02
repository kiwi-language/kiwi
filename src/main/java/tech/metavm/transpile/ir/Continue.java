package tech.metavm.transpile.ir;

import javax.annotation.Nullable;

public record Continue(
        @Nullable Label label
) implements Statement{
}

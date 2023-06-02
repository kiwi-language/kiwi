package tech.metavm.transpile.ir;

import javax.annotation.Nullable;

public record NameAndValue(
        String name,
        @Nullable IRExpression value
) {
}

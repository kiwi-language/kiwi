package tech.metavm.autograph;

import tech.metavm.expression.Expression;

import javax.annotation.Nullable;
import java.util.Map;

public record BranchInfo(
        Map<String, Expression> variables,
        @Nullable Expression yield
) {
}

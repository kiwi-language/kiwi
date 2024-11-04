package org.metavm.autograph;

import org.metavm.flow.Value;

import javax.annotation.Nullable;
import java.util.Map;

public record BranchInfo(
        Map<String, Value> variables,
        @Nullable Value yield
) {
}

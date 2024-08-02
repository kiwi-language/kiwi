package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

import javax.annotation.Nullable;

public record NodeExecResult(@Nullable Value output,
                             @Nullable ClassInstance exception,
                             @Nullable NodeRT next) {

    public static NodeExecResult jump(@Nullable Value output, @NotNull NodeRT target) {
        return new NodeExecResult(output, null, target);
    }

    public static NodeExecResult jump(@NotNull NodeRT target) {
        return new NodeExecResult(null, null, target);
    }

    public static NodeExecResult exception(@NotNull ClassInstance exception) {
        return new NodeExecResult(null, exception, null);
    }

    public static NodeExecResult ret(@Nullable Value output) {
        return new NodeExecResult(output, null, null);
    }

}

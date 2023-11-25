package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

import javax.annotation.Nullable;

public record NodeExecResult(@Nullable Instance output,
                             @Nullable ClassInstance exception,
                             @Nullable NodeRT<?> next) {

    public static NodeExecResult jump(@Nullable Instance output, @NotNull NodeRT<?> target) {
        return new NodeExecResult(output, null, target);
    }

    public static NodeExecResult jump(@NotNull NodeRT<?> target) {
        return new NodeExecResult(null, null, target);
    }

    public static NodeExecResult exception(@NotNull ClassInstance exception) {
        return new NodeExecResult(null, exception, null);
    }

    public static NodeExecResult ret(@Nullable Instance output) {
        return new NodeExecResult(output, null, null);
    }

}

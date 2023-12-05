package tech.metavm.flow;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

import javax.annotation.Nullable;

public record FlowExecResult(
        @Nullable Instance ret,
        @Nullable ClassInstance exception
) {
}

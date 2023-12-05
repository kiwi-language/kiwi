package tech.metavm.flow;

import tech.metavm.object.instance.core.ClassInstance;

public record ExceptionInfo(
        NodeRT<?> raiseNode,
        ClassInstance exception
) {
}

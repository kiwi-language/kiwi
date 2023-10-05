package tech.metavm.flow;

import tech.metavm.object.instance.ClassInstance;

public record ExceptionInfo(
        NodeRT<?> raiseNode,
        ClassInstance exception
) {
}

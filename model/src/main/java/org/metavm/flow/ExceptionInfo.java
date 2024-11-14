package org.metavm.flow;

import org.metavm.object.instance.core.ClassInstance;

public record ExceptionInfo(
        Node raiseNode,
        ClassInstance exception
) {
}

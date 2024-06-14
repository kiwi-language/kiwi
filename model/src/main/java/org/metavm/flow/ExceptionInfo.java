package org.metavm.flow;

import org.metavm.object.instance.core.ClassInstance;

public record ExceptionInfo(
        NodeRT raiseNode,
        ClassInstance exception
) {
}

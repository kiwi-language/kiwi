package org.manul.flow;

import org.manul.object.instance.core.ClassInstance;

public record ExceptionInfo(
        Node raiseNode,
        ClassInstance exception
) {
}

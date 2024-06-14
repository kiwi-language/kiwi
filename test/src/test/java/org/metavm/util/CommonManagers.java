package org.metavm.util;

import org.metavm.flow.FlowExecutionService;
import org.metavm.flow.FlowManager;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.type.TypeManager;

public record CommonManagers(
        TypeManager typeManager,
        FlowManager flowManager,
        InstanceManager instanceManager,
        FlowExecutionService flowExecutionService
) {
}

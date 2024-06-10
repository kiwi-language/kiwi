package tech.metavm.util;

import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.type.TypeManager;

public record CommonManagers(
        TypeManager typeManager,
        FlowManager flowManager,
        InstanceManager instanceManager,
        FlowExecutionService flowExecutionService
) {
}

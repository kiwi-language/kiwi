package org.metavm.util;

import org.metavm.object.instance.InstanceManager;
import org.metavm.object.type.TypeManager;
import org.metavm.task.Scheduler;
import org.metavm.task.Worker;

public record CommonManagers(
        TypeManager typeManager,
        InstanceManager instanceManager,
        Scheduler scheduler,
        Worker worker
) {
}

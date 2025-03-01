package org.metavm.util;

import org.metavm.object.type.TypeManager;
import org.metavm.task.Scheduler;
import org.metavm.task.Worker;

public record CommonManagers(
        TypeManager typeManager,
        Scheduler scheduler,
        Worker worker
) {
}

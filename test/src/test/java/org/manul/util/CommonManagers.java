package org.manul.util;

import org.manul.object.type.TypeManager;
import org.manul.task.Scheduler;
import org.manul.task.Worker;

public record CommonManagers(
        TypeManager typeManager,
        Scheduler scheduler,
        Worker worker
) {
}

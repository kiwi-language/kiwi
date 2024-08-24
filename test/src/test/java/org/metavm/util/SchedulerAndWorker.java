package org.metavm.util;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.MetaContextCache;
import org.metavm.task.Scheduler;
import org.metavm.task.Worker;

public record SchedulerAndWorker(
        Scheduler scheduler,
        Worker worker,
        MetaContextCache metaContextCache,
        EntityContextFactory entityContextFactory
) {
}

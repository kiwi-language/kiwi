package org.manul.util;

import org.manul.entity.EntityContextFactory;
import org.manul.entity.MetaContextCache;
import org.manul.task.Scheduler;
import org.manul.task.Worker;

public record SchedulerAndWorker(
        Scheduler scheduler,
        Worker worker,
        MetaContextCache metaContextCache,
        EntityContextFactory entityContextFactory
) {
}

package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.object.instance.ChangeLogManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.type.MemAllocatorStore;
import org.metavm.object.type.MemColumnStore;
import org.metavm.object.type.MemTypeTagStore;
import org.metavm.system.persistence.BlockMapper;
import org.metavm.system.persistence.RegionMapper;
import org.metavm.task.TaskManager;

public record BootstrapResult(
        DefContext defContext,
        EntityContextFactory entityContextFactory,
        EntityIdProvider idProvider,
        BlockMapper blockMapper,
        RegionMapper regionMapper,
        MemInstanceStore instanceStore,
        MemInstanceSearchServiceV2 instanceSearchService,
        MemAllocatorStore allocatorStore,
        MemColumnStore columnStore,
        MemoryStdIdStore stdIdStore,
        MemTypeTagStore typeTagStore,
        MetaContextCache metaContextCache,
        ChangeLogManager changeLogManager,
        TaskManager taskManager,
        SchedulerAndWorker schedulerAndWorker
) {
}

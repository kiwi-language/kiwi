package org.metavm.util;

import org.metavm.ddl.CommitService;
import org.metavm.entity.*;
import org.metavm.object.instance.ChangeLogManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.MemMapperRegistry;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.type.AllocatorStore;
import org.metavm.object.type.MemTypeTagStore;
import org.metavm.object.type.TypeManager;
import org.metavm.task.TaskManager;

public record BootstrapResult(
        DefContext defContext,
        EntityContextFactory entityContextFactory,
        EntityIdProvider idProvider,
        MemInstanceSearchServiceV2 instanceSearchService,
        AllocatorStore allocatorStore,
        MemoryStdIdStore stdIdStore,
        MemTypeTagStore typeTagStore,
        MetaContextCache metaContextCache,
        ChangeLogManager changeLogManager,
        TaskManager taskManager,
        SchedulerAndWorker schedulerAndWorker,
        MemMapperRegistry mapperRegistry,
        SchemaManager schemaManager,
        CommitService commitService,
        TypeManager typeManager,
        Id userId

) {
}

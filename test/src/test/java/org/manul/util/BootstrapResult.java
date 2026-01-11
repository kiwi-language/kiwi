package org.manul.util;

import org.manul.ddl.CommitService;
import org.manul.entity.*;
import org.manul.object.instance.ChangeLogManager;
import org.manul.object.instance.MemInstanceSearchServiceV2;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.persistence.MemMapperRegistry;
import org.manul.object.instance.persistence.SchemaManager;
import org.manul.object.type.AllocatorStore;
import org.manul.object.type.MemTypeTagStore;
import org.manul.object.type.TypeManager;
import org.manul.task.TaskManager;

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

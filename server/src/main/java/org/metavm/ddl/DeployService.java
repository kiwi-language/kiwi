package org.metavm.ddl;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.task.IndexRebuildTask;
import org.metavm.util.Hooks;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeployService extends EntityContextFactoryAware {

    private final SchemaManager schemaManager;
    private final InstanceSearchService instanceSearchService;

    public DeployService(SchemaManager schemaManager, InstanceSearchService instanceSearchService, EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        this.schemaManager = schemaManager;
        this.instanceSearchService = instanceSearchService;
        Hooks.CREATE_INDEX_REBUILD_TASK = this::createIndexRebuildTask;
    }

    public void revert(long appId) {
        schemaManager.revert(appId);
        instanceSearchService.revert(appId);
        Commit.META_CONTEXT_INVALIDATE_HOOK.accept(appId, false);
    }

    @Transactional
    public void createIndexRebuildTask(long appId) {
        try (var context = newContext(appId)) {
            context.bind(new IndexRebuildTask(context.allocateRootId()));
            context.finish();
        }
    }

}

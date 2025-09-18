package org.metavm.ddl;

import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.task.IndexRebuildTask;
import org.metavm.util.BusinessException;
import org.metavm.util.Hooks;
import org.metavm.util.Instances;
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

    public CommitState getDeployStatus(long appId, String commitId) {
        try (var context = newContext(appId)) {
            var commit = context.getCommit(commitId);
            return commit.getState();
        }
    }

    @Transactional
    public void abortDeployment(long appId) {
        var commitId = getOngoingCommitId(appId);
        Commit.dropTmpTableHook.accept(appId, commitId);
        try (var context = newContext(appId)) {
            var commit = context.getEntity(Commit.class, commitId);
            commit.setState(CommitState.ABORTED);
            context.finish();
        }
    }

    private Id getOngoingCommitId(long appId) {
        try (var context = newContext(appId)) {
            var commit = context.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
            if (commit == null)
                throw new BusinessException(ErrorCode.NO_ONGOING_MIGRATION);
            return commit.getId();
        }
    }

}

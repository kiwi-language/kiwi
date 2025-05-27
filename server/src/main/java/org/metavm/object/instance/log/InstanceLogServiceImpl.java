package org.metavm.object.instance.log;

import org.metavm.ddl.Commit;
import org.metavm.entity.*;
import org.metavm.object.instance.CachingInstanceStore;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;
import org.metavm.task.SyncSearchTask;
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@Component
public class InstanceLogServiceImpl extends EntityContextFactoryAware implements InstanceLogService {

    public static final Logger logger = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    private final IInstanceStore instanceStore;

    private final TransactionOperations transactionOperations;

    private final MetaContextCache metaContextCache;

    public InstanceLogServiceImpl(
            EntityContextFactory entityContextFactory,
            IInstanceStore instanceStore, TransactionOperations transactionOperations, MetaContextCache metaContextCache) {
        super(entityContextFactory);
        this.instanceStore = instanceStore;
        this.transactionOperations = transactionOperations;
        this.metaContextCache = metaContextCache;
    }

    @Override
    public void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId, DefContext defContext) {
        if (Utils.isEmpty(logs))
            return;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceLogServiceImpl.process")) {
            var instanceIds = Utils.filterAndMapUnique(logs, InstanceLog::isInsertOrUpdate, InstanceLog::getId);
            handleMigration(appId, instanceIds);
        }
    }

    @Transactional
    @Override
    public void createSearchSyncTask(long appId, Collection<Id> idsToIndex, Collection<Id> idsToRemove, DefContext defContext) {
        try(var context = newContext(appId);
            var ignored = ContextUtil.getProfiler().enter("createSearchSyncTask")) {
            WAL wal = instanceStore instanceof CachingInstanceStore cachingInstanceStore ?
                    cachingInstanceStore.getWal().copy(context.allocateRootId()) : null;
//            WAL defWal = defContext instanceof ReversedDefContext reversedDefContext ?
//                    DefContextUtils.getWal(reversedDefContext) : null;
            context.bind(new SyncSearchTask(
                    context.allocateRootId(), idsToIndex, idsToRemove, wal, /*defWal != null ? defWal.getId() : null*/ null));
//                this.instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::toVersionPO));
            context.finish();
            }
    }

    private void handleMigration(long appId, Collection<Id> instanceIds) {
        if (instanceIds.isEmpty() || ContextUtil.isDDL())
            return;
        var tracing = DebugEnv.traceMigration;
        try (var ignored = ContextUtil.getProfiler().enter("handleDDL")) {
            transactionOperations.executeWithoutResult(s -> {
                try (var context = newContext(appId, builder -> builder.timeout(0))) {
                    var commit = context.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
                    if (commit != null/* && commit.getState().isMigrating()*/) {
                        if (tracing) {
                            logger.trace("Migrating instances in real time. commit state: {}, appId: {}, instanceIds: {}",
                                    commit.getState().name(), appId, Utils.join(instanceIds, Id::toString));
                        }
                        var commitState = commit.getState();
                        var wal = commit.getWal();
                        try (var loadedContext = newContext(appId, metaContextCache.get(appId, wal.getId()),
                                builder -> builder
                                        .readWAL(wal)
                                        .relocationEnabled(commitState.isRelocationEnabled())
                                        .migrating(true)
                                        .skipPostProcessing(true)
                                        .timeout(0)
                        )
                        ) {
                            loadedContext.setDescription("DDLHandler");
                            Iterable<Instance> instances = () -> instanceIds.stream().map(loadedContext::get)
                                    .iterator();
                            Instances.migrate(instances, commit, loadedContext);
                            loadedContext.finish();
                        }
                    }
                }
            });
        }
    }

}

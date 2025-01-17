package org.metavm.object.instance.log;

import org.metavm.ddl.Commit;
import org.metavm.entity.*;
import org.metavm.object.instance.CachingInstanceStore;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.version.Version;
import org.metavm.object.version.VersionRepository;
import org.metavm.object.version.Versions;
import org.metavm.task.PublishMetadataEventTask;
import org.metavm.task.SynchronizeSearchTask;
import org.metavm.util.ContextUtil;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
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
        WAL.setPostProcessHook((appId, logs) -> process(appId, logs, instanceStore, List.of(), null, ModelDefRegistry.getDefContext()));
    }

    @Override
    public void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, List<Id> migrated, @Nullable String clientId, DefContext defContext) {
        if (Utils.isEmpty(logs) && migrated.isEmpty())
            return;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceLogServiceImpl.process")) {
            var newInstanceIds = Utils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
            handleDDL(appId, newInstanceIds);
            handleMetaChanges(appId, logs, clientId);
        }
    }

    @Transactional
    @Override
    public void createSearchSyncTask(long appId, Collection<Id> idsToIndex, Collection<Id> idsToRemove, DefContext defContext) {
        try(var context = newContext(appId);
            var ignored = ContextUtil.getProfiler().enter("createSearchSyncTask")) {
            WAL wal = instanceStore instanceof CachingInstanceStore cachingInstanceStore ?
                    cachingInstanceStore.getWal().copy() : null;
//            WAL defWal = defContext instanceof ReversedDefContext reversedDefContext ?
//                    DefContextUtils.getWal(reversedDefContext) : null;
            context.bind(new SynchronizeSearchTask(
                    idsToIndex, idsToRemove, wal, /*defWal != null ? defWal.getId() : null*/ null));
//                this.instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::toVersionPO));
            context.finish();
            }
    }

    private void handleMetaChanges(long appId, List<InstanceLog> logs, @Nullable String clientId) {
        try (var ignored = ContextUtil.getProfiler().enter("handleMetaChanges")){
            var changedTypeDefIds = new HashSet<String>();
            var changedFunctionIds = new HashSet<String>();
            var removedTypeDefIds = new HashSet<String>();
            var removedFunctionIds = new HashSet<String>();
            for (InstanceLog log : logs) {
                var id = log.getId();
                var entityTag = log.getEntityTag();
                if (entityTag == EntityRegistry.TAG_Klass) {
                    if (log.isDelete())
                        removedTypeDefIds.add(id.toString());
                    else
                        changedTypeDefIds.add(id.toString());
                } else if (entityTag == EntityRegistry.TAG_Function) {
                    if (log.isDelete())
                        removedFunctionIds.add(id.toString());
                    else
                        changedFunctionIds.add(id.toString());
                }
            }
            if (!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()
                    || !changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
                transactionOperations.executeWithoutResult(s -> {
                    try (var context = newContext(appId, builder -> builder.timeout(0))) {
                        context.setDescription("MetaChange");
                        var v = Versions.create(
                                changedTypeDefIds,
                                removedTypeDefIds,
                                changedFunctionIds,
                                removedFunctionIds,
                                new VersionRepository() {
                                    @Nullable
                                    @Override
                                    public Version getLastVersion() {
                                        return Utils.first(
                                                context.query(Version.IDX_VERSION.newQueryBuilder().limit(1).desc(true).build())
                                        );
                                    }

                                    @Override
                                    public void save(Version version) {
                                        context.bind(version);
                                    }
                                });
                        if (!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty() || !changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
                            context.bind(new PublishMetadataEventTask(changedTypeDefIds, removedTypeDefIds, changedFunctionIds, removedFunctionIds, v.getVersion(), clientId));
                        }
                        context.finish();
                    }
                });
            }
        }
    }

    private void handleDDL(long appId, Collection<Id> instanceIds) {
        if (instanceIds.isEmpty() || ContextUtil.isDDL())
            return;
        try (var ignored = ContextUtil.getProfiler().enter("handleDDL")) {
            transactionOperations.executeWithoutResult(s -> {
                try (var context = newContext(appId, builder -> builder.timeout(0))) {
                    var commit = context.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
                    if (commit != null) {
                        var commitState = commit.getState();
                        var wal = commitState.isPreparing() ? commit.getWal() : null;
                        try (var loadedContext = newContext(appId,
                                metaContextCache.get(appId, wal != null ? wal.getId() : null),
                                builder -> builder
                                        .readWAL(wal)
                                        .relocationEnabled(commitState.isRelocationEnabled())
                                        .timeout(0)
                        )
                        ) {
                            loadedContext.setDescription("DDLHandler");
                            Iterable<Instance> instances = () -> instanceIds.stream().map(loadedContext::get)
                                    .iterator();
                            commit.getState().process(instances, commit, loadedContext);
                            loadedContext.finish();
                        }
                    }
                }
            });
        }
    }

}

package org.metavm.object.instance.log;

import org.metavm.ddl.Commit;
import org.metavm.ddl.DefContextUtils;
import org.metavm.entity.*;
import org.metavm.flow.Function;
import org.metavm.object.instance.CachingInstanceStore;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.TypeDef;
import org.metavm.object.version.Version;
import org.metavm.object.version.VersionRepository;
import org.metavm.object.version.Versions;
import org.metavm.object.view.Mapping;
import org.metavm.task.PublishMetadataEventTask;
import org.metavm.task.SynchronizeSearchTask;
import org.metavm.util.NncUtils;
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
        if (NncUtils.isEmpty(logs) && migrated.isEmpty())
            return;
        var newInstanceIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        handleDDL(appId, newInstanceIds);
        handleMetaChanges(appId, logs, clientId);
    }

    @Transactional
    @Override
    public void createSearchSyncTask(long appId, List<Id> changedIds, List<Id> removedIds, DefContext defContext) {
        try(var context = newContext(appId)) {
            WAL wal = instanceStore instanceof CachingInstanceStore cachingInstanceStore ?
                    cachingInstanceStore.getWal().copy() : null;
            WAL defWal = defContext instanceof ReversedDefContext reversedDefContext ?
                    DefContextUtils.getWal(reversedDefContext).copy() : null;
            context.bind(new SynchronizeSearchTask(
                    NncUtils.map(changedIds, Identifier::fromId),
                    NncUtils.map(removedIds, Identifier::fromId),
                    wal, defWal != null ? Identifier.fromId(defWal.getId()) : null));
//                this.instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::toVersionPO));
            context.finish();
            }
    }

    private void handleMetaChanges(long appId, List<InstanceLog> logs, @Nullable String clientId) {
        var changedTypeDefIds = new HashSet<String>();
        var changedMappingIds = new HashSet<String>();
        var changedFunctionIds = new HashSet<String>();
        var removedTypeDefIds = new HashSet<String>();
        var removedMappingIds = new HashSet<String>();
        var removedFunctionIds = new HashSet<String>();
        for (InstanceLog log : logs) {
            var id = log.getId();
            if (id instanceof TaggedPhysicalId tpId && tpId.getTypeTag() > 4) {
                var defContext = ModelDefRegistry.getDefContext();
                var mapper = defContext.tryGetMapper(tpId.getTypeTag());
                if(mapper != null) {
                    var javaClass = mapper.getEntityClass();
                    if (TypeDef.class.isAssignableFrom(javaClass)) {
                        if (log.isDelete())
                            removedTypeDefIds.add(id.toString());
                        else
                            changedTypeDefIds.add(id.toString());
                    } else if (Mapping.class.isAssignableFrom(javaClass)) {
                        if (log.isDelete())
                            removedMappingIds.add(id.toString());
                        else
                            changedMappingIds.add(id.toString());
                    } else if (Function.class.isAssignableFrom(javaClass)) {
                        if (log.isDelete())
                            removedFunctionIds.add(id.toString());
                        else
                            changedFunctionIds.add(id.toString());
                    }
                }
            }
        }
        if (!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()
                || !changedMappingIds.isEmpty() || !removedMappingIds.isEmpty()
                || !changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
            transactionOperations.executeWithoutResult(s -> {
                try (var context = newContext(appId, builder -> builder.timeout(0))) {
                    context.getInstanceContext().setDescription("MetaChange");
                    var v = Versions.create(
                            changedTypeDefIds,
                            removedTypeDefIds,
                            changedMappingIds,
                            removedMappingIds,
                            changedFunctionIds,
                            removedFunctionIds,
                            new VersionRepository() {
                                @Nullable
                                @Override
                                public Version getLastVersion() {
                                    return NncUtils.first(
                                            context.query(Version.IDX_VERSION.newQueryBuilder().limit(1).desc(true).build())
                                    );
                                }

                                @Override
                                public void save(Version version) {
                                    context.bind(version);
                                }
                            });
                    if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty() || !changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
                        context.bind(new PublishMetadataEventTask(changedTypeDefIds, removedTypeDefIds, changedFunctionIds, removedFunctionIds, v.getVersion(), clientId));
                    }
                    context.finish();
                }
            });
        }
    }

    private void handleDDL(long appId, Collection<Id> instanceIds) {
        if (instanceIds.isEmpty())
            return;
        transactionOperations.executeWithoutResult(s -> {
            try (var context = newContext(appId, builder -> builder.timeout(0))) {
                var commit = context.selectFirstByKey(Commit.IDX_RUNNING, true);
                if (commit != null) {
                    var commitState = commit.getState();
                    var wal = commitState.isPreparing() ? commit.getWal() : null;
                    try (var loadedContext = newContext(appId,
                            metaContextCache.get(appId, wal != null? wal.getId() : null),
                            builder -> builder
                            .readWAL(wal)
                            .relocationEnabled(commitState.isRelocationEnabled())
                            .timeout(0)
                    )
                    ) {
                        loadedContext.getInstanceContext().setDescription("DDLHandler");
                        Iterable<Instance> instances = () -> instanceIds.stream().map(loadedContext.getInstanceContext()::get)
                                .iterator();
                        commit.getState().process(instances, commit, loadedContext);
                        loadedContext.finish();
                    }
                }
            }
        });
    }

    private <T extends Entity> boolean invokeHandler(List<ClassInstance> instances, LogHandler<T> handler,
                                                  @Nullable String clientId, IEntityContext context) {
        var type = context.getDefContext().getClassType(handler.getEntityClass());
        var entities = NncUtils.filterAndMap(instances, i -> type.isInstance(i.getReference()),
                i -> context.getEntity(handler.getEntityClass(), i));
        if (!entities.isEmpty()) {
            handler.process(entities, clientId, context, entityContextFactory);
            return true;
        }
        else
            return false;
    }

}

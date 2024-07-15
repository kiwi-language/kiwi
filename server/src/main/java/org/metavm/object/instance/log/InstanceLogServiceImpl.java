package org.metavm.object.instance.log;

import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.*;
import org.metavm.event.EventQueue;
import org.metavm.event.rest.dto.FunctionChangeEvent;
import org.metavm.event.rest.dto.TypeChangeEvent;
import org.metavm.flow.Function;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TaggedPhysicalId;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.type.TypeDef;
import org.metavm.object.version.Version;
import org.metavm.object.version.VersionRepository;
import org.metavm.object.version.Versions;
import org.metavm.object.view.Mapping;
import org.metavm.task.ForwardedFlagSetter;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
public class InstanceLogServiceImpl extends EntityContextFactoryAware implements InstanceLogService {

    public static final Logger logger = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    private final InstanceSearchService instanceSearchService;

    private final IInstanceStore instanceStore;

    private final TransactionOperations transactionOperations;

    private final List<LogHandler<?>> handlers;
    private final EventQueue eventQueue;

    public InstanceLogServiceImpl(
            EntityContextFactory entityContextFactory,
            InstanceSearchService instanceSearchService,
            IInstanceStore instanceStore, TransactionOperations transactionOperations, List<LogHandler<?>> handlers, EventQueue eventQueue) {
        super(entityContextFactory);
        this.instanceSearchService = instanceSearchService;
        this.instanceStore = instanceStore;
        this.transactionOperations = transactionOperations;
        this.handlers = handlers;
        this.eventQueue = eventQueue;
        WAL.setPostProcessHook((appId, logs) -> process(appId, logs, instanceStore, List.of(), null));
    }

    @Override
    public void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, List<Id> migrated, @Nullable String clientId) {
        if (NncUtils.isEmpty(logs) && migrated.isEmpty())
            return;
        List<Id> idsToLoad = NncUtils.filterAndMap(logs, InstanceLog::isInsertOrUpdate, InstanceLog::getId);
        var newInstanceIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        handleDDL(appId, newInstanceIds);
        handleMigration(appId, migrated);
        handleMetaChanges(appId, logs, clientId);
        try (var context = newContextWithStore(appId, instanceStore)) {
            var instanceContext = context.getInstanceContext();
            List<ClassInstance> changed = NncUtils.filterByType(instanceContext.batchGet(idsToLoad), ClassInstance.class);
            List<ClassInstance> created = NncUtils.filter(changed, c -> newInstanceIds.contains(c.getId()));
            for (LogHandler<?> handler : handlers) {
                invokeHandler(created, handler, clientId, context);
            }
            List<Id> removed = NncUtils.filterAndMap(logs, InstanceLog::isDelete, InstanceLog::getId);
            if (NncUtils.isNotEmpty(changed) || NncUtils.isNotEmpty(removed)) {
                try (var ignored = context.getProfiler().enter("bulk")) {
                    instanceSearchService.bulk(appId, changed, removed);
                }
            }
            this.instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::toVersionPO));
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
                var javaClass = defContext.getJavaClassByTag(tpId.getTypeTag());
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
        if (!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()
                || !changedMappingIds.isEmpty() || !removedMappingIds.isEmpty()
                || !changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
            var version = transactionOperations.execute(s -> {
                try (var context = newContext(appId)) {
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
                    context.finish();
                    return v;
                }
            });
            assert version != null;
            if (!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
                eventQueue.publishAppEvent(
                        new TypeChangeEvent(appId, version.getVersion(), NncUtils.merge(changedTypeDefIds, removedTypeDefIds), clientId)
                );
            }
            if (!changedFunctionIds.isEmpty() || !removedFunctionIds.isEmpty()) {
                eventQueue.publishAppEvent(
                        new FunctionChangeEvent(appId, version.getVersion(), NncUtils.merge(changedFunctionIds, removedFunctionIds), clientId)
                );
            }
        }
    }

    private void handleDDL(long appId, Collection<Id> instanceIds) {
        if (instanceIds.isEmpty())
            return;
        try (var context = newContext(appId)) {
            var commit = context.selectFirstByKey(Commit.IDX_STATE, CommitState.RUNNING);
            if (commit != null) {
                transactionOperations.executeWithoutResult(s -> {
                    try (var loadedContext = entityContextFactory.newLoadedContext(appId, commit.getWal(), true)) {
                        Iterable<ClassInstance> instances = () -> instanceIds.stream().map(loadedContext.getInstanceContext()::get)
                                .filter(i -> i instanceof ClassInstance)
                                .map(i -> (ClassInstance) i)
                                .iterator();
                        Instances.applyDDL(instances, commit, loadedContext);
                        loadedContext.finish();
                    }
                });
            }
        }
    }

    private void handleMigration(long appId, List<Id> migrated) {
        if(migrated.isEmpty())
            return;
        transactionOperations.executeWithoutResult(s -> {
            try(var context = newContext(appId)) {
                for (Id id : migrated) {
                    context.bind(new ForwardedFlagSetter(id.toString()));
                }
                context.finish();
            }
        });
    }

    private <T extends Entity> void invokeHandler(List<ClassInstance> instances, LogHandler<T> handler,
                                                  @Nullable String clientId, IEntityContext context) {
        var type = context.getDefContext().getClassType(handler.getEntityClass());
        var entities = NncUtils.filterAndMap(instances, i -> type.isInstance(i.getReference()),
                i -> context.getEntity(handler.getEntityClass(), i));
        if (!entities.isEmpty())
            handler.process(entities, clientId, context, entityContextFactory);
    }

}

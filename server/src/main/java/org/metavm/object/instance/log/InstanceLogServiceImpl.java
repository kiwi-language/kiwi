package org.metavm.object.instance.log;

import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.type.Field;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static org.metavm.util.Instances.computeFieldInitialValue;

@Component
public class InstanceLogServiceImpl extends EntityContextFactoryAware implements InstanceLogService {

    public static final Logger logger = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    private final InstanceSearchService instanceSearchService;

    private final IInstanceStore instanceStore;

    private final List<LogHandler<?>> handlers;

    public InstanceLogServiceImpl(
            EntityContextFactory entityContextFactory,
            InstanceSearchService instanceSearchService,
            IInstanceStore instanceStore, List<LogHandler<?>> handlers) {
        super(entityContextFactory);
        this.instanceSearchService = instanceSearchService;
        this.instanceStore = instanceStore;
        this.handlers = handlers;
        WAL.setPostProcessHook(logs -> process(logs, instanceStore, null));
    }

    @Override
    public void process(List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId) {
        if (NncUtils.isEmpty(logs)) {
            return;
        }
        var appId = logs.get(0).getAppId();
        List<Id> idsToLoad = NncUtils.filterAndMap(logs, InstanceLog::isInsertOrUpdate, InstanceLog::getId);
        var newInstanceIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        handleDDL(appId, newInstanceIds);
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

    private void handleDDL(long appId, Collection<Id> instanceIds) {
        if(instanceIds.isEmpty())
            return;
        try (var context = newContext(appId)) {
            var commit = context.selectFirstByKey(Commit.IDX_STATE, CommitState.RUNNING);
            if (commit != null) {
                try (var loadedContext = entityContextFactory.newLoadedContext(appId, commit.getWal())) {
                    var instances = NncUtils.mapAndFilterByType(instanceIds, loadedContext.getInstanceContext()::get, ClassInstance.class);
                    var fields = NncUtils.map(commit.getNewFieldIds(), loadedContext::getField);
                    for (var instance : instances) {
                        for (Field field : fields) {
                            if (field.getDeclaringType().getType().isInstance(instance)) {
                                var initialValue = computeFieldInitialValue(instance, field, loadedContext.getInstanceContext());
                                instance.setField(field, initialValue);
                            }
                        }
                    }
                    loadedContext.finish();
                }
            }
        }
    }

    private <T extends Entity> void invokeHandler(List<ClassInstance> instances, LogHandler<T> handler,
                                                  @Nullable String clientId, IEntityContext context) {
        var type = context.getDefContext().getClassType(handler.getEntityClass());
        var entities = NncUtils.filterAndMap(instances, type::isInstance,
                i -> context.getEntity(handler.getEntityClass(), i));
        if (!entities.isEmpty())
            handler.process(entities, clientId, context, entityContextFactory);
    }

}

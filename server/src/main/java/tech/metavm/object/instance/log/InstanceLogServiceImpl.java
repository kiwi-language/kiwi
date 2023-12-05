package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContextFactory;
import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Component
public class InstanceLogServiceImpl implements InstanceLogService {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    private final InstanceSearchService instanceSearchService;

    private final IInstanceContextFactory instanceContextFactory;

    private final IInstanceStore instanceStore;

    private final List<LogHandler<?>> handlers;

    public InstanceLogServiceImpl(InstanceSearchService instanceSearchService,
                                  IInstanceContextFactory instanceContextFactory,
                                  IInstanceStore instanceStore, List<LogHandler<?>> handlers) {
        this.instanceSearchService = instanceSearchService;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceStore = instanceStore;
        this.handlers = handlers;
    }

    @Override
    public void process(List<InstanceLog> logs, @Nullable String clientId) {
        if (NncUtils.isEmpty(logs)) {
            return;
        }
        long appId = logs.get(0).getAppId();
        List<Long> idsToLoad = NncUtils.filterAndMap(
                logs,
                InstanceLog::isInsertOrUpdate,
                InstanceLog::getId
        );
        Set<Long> newInstanceIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        try (var context = newContext(appId)) {
            var instanceContext = context.getInstanceContext();
            List<ClassInstance> changed = NncUtils.filterByType(instanceContext.batchGet(idsToLoad), ClassInstance.class);
            List<ClassInstance> created = NncUtils.filter(changed, c -> newInstanceIds.contains(c.getIdRequired()));
            for (LogHandler<?> handler : handlers) {
                invokeHandler(created, handler, clientId, context);
            }
            List<Long> removed = NncUtils.filterAndMap(logs, InstanceLog::isDelete, InstanceLog::getId);
            if (NncUtils.isNotEmpty(changed) || NncUtils.isNotEmpty(removed)) {
                try (var ignored = context.getProfiler().enter("bulk")) {
                    instanceSearchService.bulk(appId, changed, removed);
                }
            }
            instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
        }
    }

    private <T extends Entity> void invokeHandler(List<ClassInstance> instances, LogHandler<T> handler,
                                                  @Nullable String clientId,  IEntityContext context) {
        var type = context.getDefContext().getClassType(handler.getEntityClass());
        var entities = NncUtils.filterAndMap(instances, type::isInstance,
                i -> context.getEntity(handler.getEntityClass(), i));
        if(!entities.isEmpty())
            handler.process(entities, clientId, context);
    }

    private IEntityContext newContext(long appId) {
        return instanceContextFactory.newEntityContext(appId, false);
    }

}

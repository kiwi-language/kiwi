package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Component
public class InstanceLogServiceImpl extends EntityContextFactoryBean implements InstanceLogService {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

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
    }

    @Override
    public void process(List<InstanceLog> logs, @Nullable String clientId) {
        if (NncUtils.isEmpty(logs)) {
            return;
        }
        var appId = logs.get(0).getAppId();
        List<Id> idsToLoad = NncUtils.filterAndMap(logs, InstanceLog::isInsertOrUpdate, InstanceLog::getInstanceId);
        Set<Long> newInstanceIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        try (var context = newContext(appId)) {
            var instanceContext = context.getInstanceContext();
            List<ClassInstance> changed = NncUtils.filterByType(instanceContext.batchGet(idsToLoad), ClassInstance.class);
            List<ClassInstance> created = NncUtils.filter(changed, c -> newInstanceIds.contains(c.getPhysicalId()));
            for (LogHandler<?> handler : handlers) {
                invokeHandler(created, handler, clientId, context);
            }
            List<Id> removed = NncUtils.filterAndMap(logs, InstanceLog::isDelete, InstanceLog::getInstanceId);
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

}

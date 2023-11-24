package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.websocket.MetaChangeQueue;
import tech.metavm.object.version.Version;
import tech.metavm.task.Task;
import tech.metavm.task.TaskSignal;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InstanceLogServiceImpl implements InstanceLogService {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    private final InstanceSearchService instanceSearchService;

    private final IInstanceContextFactory instanceContextFactory;

    private final IInstanceStore instanceStore;

    private final TransactionOperations transactionOperations;

    private final MetaChangeQueue metaChangeQueue;

    public InstanceLogServiceImpl(InstanceSearchService instanceSearchService,
                                  IInstanceContextFactory instanceContextFactory,
                                  IInstanceStore instanceStore, TransactionOperations transactionOperations,
                                  MetaChangeQueue metaChangeQueue) {
        this.instanceSearchService = instanceSearchService;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceStore = instanceStore;
        this.transactionOperations = transactionOperations;
        this.metaChangeQueue = metaChangeQueue;
    }

    @Override
    public void process(List<InstanceLog> logs, @Nullable String clientId) {
        if (NncUtils.isEmpty(logs)) {
            return;
        }
        long tenantId = logs.get(0).getTenantId();
        List<Long> idsToLoad = NncUtils.filterAndMap(
                logs,
                InstanceLog::isInsertOrUpdate,
                InstanceLog::getId
        );
        Set<Long> insertIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        try (var context = newContext(tenantId)) {
            var instanceContext = context.getInstanceContext();
            List<ClassInstance> changed = NncUtils.filterByType(instanceContext.batchGet(idsToLoad), ClassInstance.class);
            ClassType taskType = ModelDefRegistry.getClassType(Task.class);
            List<ClassInstance> taskInstances = NncUtils.filter(
                    changed, inst -> insertIds.contains(inst.getId()) && taskType.isInstance(inst)
            );
            if (NncUtils.isNotEmpty(taskInstances)) {
                increaseUnfinishedTaskCount(tenantId, taskInstances.size());
            }
            handleVersions(changed, clientId,  context);
            List<Long> removed = NncUtils.filterAndMap(logs, InstanceLog::isDelete, InstanceLog::getId);
            if (NncUtils.isNotEmpty(changed) || NncUtils.isNotEmpty(removed)) {
                try (var ignored = context.getProfiler().enter("bulk")) {
                    instanceSearchService.bulk(tenantId, changed, removed);
                }
            }
            instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
        }
    }

    private void handleVersions(List<ClassInstance> changed, @Nullable String clientId, IEntityContext context) {
        ClassType versionType = ModelDefRegistry.getClassType(Version.class);
        List<Version> versions = NncUtils.filterAndMap(changed,
                versionType::isInstance, inst -> context.getEntity(Version.class, inst));
        if(!versions.isEmpty()) {
            long maxVersion = 0L;
            Set<Long> typeIds = new HashSet<>();
            for (Version version : versions) {
                maxVersion = Math.max(maxVersion, version.getVersion());
                typeIds.addAll(version.getRemovedTypeIds());
                typeIds.addAll(version.getChangeTypeIds());
            }
            metaChangeQueue.notifyTypeChange(context.getTenantId(), maxVersion,
                    new ArrayList<>(typeIds), clientId);
        }
    }


    private void increaseUnfinishedTaskCount(long tenantId, int newJobCount) {
        transactionOperations.executeWithoutResult(s -> {
            try (var rootContext = newRootContext()) {
                TaskSignal signal = NncUtils.requireNonNull(rootContext.selectByUniqueKey(TaskSignal.IDX_TENANT_ID, tenantId));
                signal.setUnfinishedCount(signal.getUnfinishedCount() + newJobCount);
                signal.setLastTaskCreatedAt(System.currentTimeMillis());
                rootContext.finish();
            }
        });
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newRootEntityContext(false);
    }

    private IEntityContext newContext(long tenantId) {
        return instanceContextFactory.newEntityContext(tenantId, false);
    }

}

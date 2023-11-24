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

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class InstanceLogServiceImpl implements InstanceLogService {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLogServiceImpl.class);

    public static final int CORE_POOL_SIZE = 32;
    public static final int MAX_POOL_SIZE = 32;
    public static final long KEEP_ALIVE = 10000L;
    public static final int QUEUE_SIZE = 1024;

    private final InstanceSearchService instanceSearchService;

    private final IInstanceContextFactory instanceContextFactory;

    private final IInstanceStore instanceStore;

    private final TransactionOperations transactionOperations;

    private final MetaChangeQueue metaChangeQueue;

    private final Executor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE)
    );

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
    public void asyncProcess(List<InstanceLog> logs) {
        try {
            executor.execute(() -> process(logs));
        } catch (RejectedExecutionException e) {
            LOGGER.error("task rejected by thread pool", e);
        }
    }

    @Override
    public void process(List<InstanceLog> logs) {
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
            ClassType versionType = ModelDefRegistry.getClassType(Version.class);
            List<Version> versions = NncUtils.filterAndMap(changed,
                    versionType::isInstance, inst -> context.getEntity(Version.class, inst));
            handleVersions(tenantId, versions);
            List<Long> removed = NncUtils.filterAndMap(logs, InstanceLog::isDelete, InstanceLog::getId);
            if (NncUtils.isNotEmpty(changed) || NncUtils.isNotEmpty(removed)) {
                try (var ignored = context.getProfiler().enter("bulk")) {
                    instanceSearchService.bulk(tenantId, changed, removed);
                }
            }
            instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
        }
    }

    private void handleVersions(long tenantId, List<Version> versions) {
        var maxVersion = versions.stream().mapToLong(Version::getVersion).max().orElse(-1L);
        if(maxVersion != -1L) {
            metaChangeQueue.notifyTypeChange(tenantId, maxVersion);
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

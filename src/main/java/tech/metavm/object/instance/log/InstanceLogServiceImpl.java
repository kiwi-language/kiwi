package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.task.Task;
import tech.metavm.task.TaskSignal;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.meta.ClassType;
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

    private final Executor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE)
    );

    public InstanceLogServiceImpl(InstanceSearchService instanceSearchService,
                                  IInstanceContextFactory instanceContextFactory,
                                  IInstanceStore instanceStore) {
        this.instanceSearchService = instanceSearchService;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceStore = instanceStore;
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
        try (var context = instanceContextFactory.newBuilder()
                .tenantId(tenantId).build()) {
            var instanceContext = context.getInstanceContext();
            List<ClassInstance> toIndex = NncUtils.filterByType(instanceContext.batchGet(idsToLoad), ClassInstance.class);
            ClassType taskType = ModelDefRegistry.getClassType(Task.class);
            List<ClassInstance> taskInstances = NncUtils.filter(
                    toIndex, inst -> insertIds.contains(inst.getId()) && taskType.isInstance(inst)
            );
            if (NncUtils.isNotEmpty(taskInstances)) {
                increaseUnfinishedTaskCount(tenantId, taskInstances.size());
            }
            List<Long> toDelete = NncUtils.filterAndMap(
                    logs,
                    InstanceLog::isDelete,
                    InstanceLog::getId
            );
            if (NncUtils.isNotEmpty(toIndex) || NncUtils.isNotEmpty(toDelete)) {
                try(var ignored = context.getProfiler().enter("bulk")) {
                    instanceSearchService.bulk(tenantId, toIndex, toDelete);
                }
            }
            instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
        }
    }

    private void increaseUnfinishedTaskCount(long tenantId, int newJobCount) {
        try (var rootContext = instanceContextFactory.newRootEntityContext(false)) {
            TaskSignal signal = NncUtils.requireNonNull(rootContext.selectByUniqueKey(TaskSignal.IDX_TENANT_ID, tenantId));
            signal.setUnfinishedCount(signal.getUnfinishedCount() + newJobCount);
            signal.setLastTaskCreatedAt(System.currentTimeMillis());
            rootContext.finish();
        }
    }

}

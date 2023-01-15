package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.IInstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.job.Job;
import tech.metavm.job.JobSignal;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.Constants;
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
        }
        catch (RejectedExecutionException e) {
            LOGGER.error("task rejected by thread pool", e);
        }
    }

    @Override
    public void process(List<InstanceLog> logs) {
        if(NncUtils.isEmpty(logs)) {
            return;
        }
        long tenantId = logs.get(0).getTenantId();
        List<Long> idsToLoad = NncUtils.filterAndMap(
                logs,
                InstanceLog::isInsertOrUpdate,
                InstanceLog::getId
        );
        Set<Long> insertIds = NncUtils.filterAndMapUnique(logs, InstanceLog::isInsert, InstanceLog::getId);
        IInstanceContext context = instanceContextFactory.newContext(tenantId);

        List<ClassInstance> toIndex = NncUtils.filterByType(context.batchGet(idsToLoad), ClassInstance.class);
        ClassType jobType = ModelDefRegistry.getClassType(Job.class);
        List<ClassInstance> jobInstances = NncUtils.filter(
                toIndex, inst -> insertIds.contains(inst.getId()) && jobType.isInstance(inst)
        );
        if(NncUtils.isNotEmpty(jobInstances)) {
            increaseUnfinishedJobCount(tenantId, jobInstances.size());
        }
        List<Long> toDelete = NncUtils.filterAndMap(
                logs,
                InstanceLog::isDelete,
                InstanceLog::getId
        );
        instanceSearchService.bulk(tenantId, toIndex, toDelete);
        instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
    }

    private void increaseUnfinishedJobCount(long tenantId, int newJobCount) {
        IEntityContext rootContext = instanceContextFactory.newRootContext().getEntityContext();
        JobSignal signal = rootContext.selectByUniqueKey(JobSignal.IDX_TENANT_ID, tenantId);
        signal.setUnfinishedCount(signal.getUnfinishedCount() + newJobCount);
        rootContext.finish();
    }

}

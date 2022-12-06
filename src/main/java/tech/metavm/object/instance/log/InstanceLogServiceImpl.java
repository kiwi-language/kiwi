package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.IInstanceContextFactory;
import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.util.NncUtils;

import java.util.List;
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
        IInstanceContext context = instanceContextFactory.newContext(tenantId);
        List<Instance> toIndex = context.batchGet(idsToLoad);
        List<Long> toDelete = NncUtils.filterAndMap(
                logs,
                InstanceLog::isDelete,
                InstanceLog::getId
        );
        instanceSearchService.bulk(tenantId, toIndex, toDelete);
        instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
    }


}

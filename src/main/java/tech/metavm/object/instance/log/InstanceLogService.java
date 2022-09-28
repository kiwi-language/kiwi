package tech.metavm.object.instance.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.concurrent.*;

@Component
public class InstanceLogService {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLogService.class);

    public static final int CORE_POOL_SIZE = 32;
    public static final int MAX_POOL_SIZE = 32;
    public static final long KEEP_ALIVE = 10000L;
    public static final int QUEUE_SIZE = 1024;

    @Autowired
    private InstanceSearchService instanceSearchService;

    @Autowired
    private InstanceManager instanceManager;

    private final Executor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE)
    );

    public void asyncProcess(List<InstanceLog> logs) {
        try {
            executor.execute(() -> process(logs));
        }
        catch (RejectedExecutionException e) {
            LOGGER.error("task rejected by thread pool", e);
        }
    }

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
        List<InstanceDTO> toIndex = instanceManager.batchGet(tenantId, idsToLoad);
        List<Long> toDelete = NncUtils.filterAndMap(
                logs,
                InstanceLog::isDelete,
                InstanceLog::getId
        );
        instanceSearchService.bulk(tenantId, toIndex, toDelete);
        instanceManager.onSyncSuccess(logs);
    }

}

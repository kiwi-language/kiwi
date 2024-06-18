package org.metavm.autograph;

import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.object.instance.rest.InstanceVersionsRequest;
import org.metavm.object.instance.rest.TreeDTO;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.system.BlockManager;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class MockTypeClient implements TypeClient {

    public static final Logger logger = LoggerFactory.getLogger(MockTypeClient.class);

    private final TypeManager typeManager;
    private final BlockManager blockManager;
    private final InstanceManager instanceManager;
    private final ExecutorService executor;
    private final TransactionOperations transactionOperations;

    public MockTypeClient(TypeManager typeManager, BlockManager blockManager,
                          InstanceManager instanceManager, ExecutorService executor,
                          TransactionOperations transactionOperations) {
        this.typeManager = typeManager;
        this.blockManager = blockManager;
        this.instanceManager = instanceManager;
        this.executor = executor;
        this.transactionOperations = transactionOperations;
    }

    @Override
    public long getAppId() {
        return submit((ContextUtil::getAppId), "getAppId");
    }

    @Override
    public void setAppId(long appId) {
        submit(() -> ContextUtil.setAppId(appId));
    }

    @Override
    public void batchSave(BatchSaveRequest request) {
        submit(() -> transactionOperations.execute((TransactionCallback<Object>) status -> typeManager.batchSave(request)), "batchSave");
    }

    @Override
    public void login(long appId, String loginName, String password) {
        submit(() -> ContextUtil.setAppId(appId));
    }

    @Override
    public BlockDTO getContainingBlock(long id) {
        return submit(() -> blockManager.getContaining(id), "getContainingBlock");
    }

    @Override
    public List<BlockDTO> getActive(List<Long> typeIds) {
        return submit(() -> blockManager.getActive(typeIds), "getActiveBlocks");
    }

    @Override
    public List<TreeVersion> getVersions(InstanceVersionsRequest request) {
        return submit(() -> instanceManager.getVersions(request.ids()), "getVersions");
    }

    @Override
    public List<TreeDTO> getTrees(GetTreesRequest request) {
        return submit(() -> instanceManager.getTrees(request.ids()), "getTrees");
    }

    @Override
    public TreeResponse queryTrees(TypeTreeQuery query) {
        return submit(() -> typeManager.queryTrees(query), "queryTrees");
    }

    private void submit(Runnable task) {
        try {
            executor.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T submit(Callable<T> callable, String taskName) {
        try {
            return executor.submit(() -> {
                ContextUtil.resetProfiler();
                try (var ignored = ContextUtil.getProfiler().enter(taskName)) {
                    return callable.call();
                } finally {
                    var result = ContextUtil.getProfiler().finish(false, true);
                    if(result.duration() > 1000000L)
                        logger.info(result.output());
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}

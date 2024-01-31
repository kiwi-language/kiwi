package tech.metavm.autograph;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.rest.GetTreesRequest;
import tech.metavm.object.instance.rest.InstanceVersionDTO;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.object.instance.rest.TreeDTO;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.object.type.rest.dto.TypeTreeResponse;
import tech.metavm.system.BlockManager;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.util.ContextUtil;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class MockTypeClient implements TypeClient {

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
        return submit(ContextUtil::getAppId);
    }

    @Override
    public void setAppId(long appId) {
        submit(() -> ContextUtil.setAppId(appId));
    }

    @Override
    public void batchSave(BatchSaveRequest request) {
        submit(() -> transactionOperations.execute(
                (TransactionCallback<Object>) status -> typeManager.batchSave(request))
        );
    }

    @Override
    public void login(long appId, String loginName, String password) {
        submit(() -> ContextUtil.setAppId(appId));
    }

    @Override
    public BlockDTO getContainingBlock(long id) {
        return submit(() -> blockManager.getContaining(id));
    }

    @Override
    public List<BlockDTO> getActive(List<Long> typeIds) {
        return submit(() -> blockManager.getActive(typeIds));
    }

    @Override
    public List<InstanceVersionDTO> getVersions(InstanceVersionsRequest request) {
        return submit(() -> instanceManager.getVersions(request.ids()));
    }

    @Override
    public List<TreeDTO> getTrees(GetTreesRequest request) {
        return submit(() -> instanceManager.getTrees(request.ids()));
    }

    @Override
    public TypeTreeResponse queryTrees(TypeTreeQuery query) {
        return submit(() -> typeManager.queryTypeTrees(query));
    }

    private void submit(Runnable task) {
        try {
            executor.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T submit(Callable<T> callable) {
        try {
            return executor.submit(callable).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}

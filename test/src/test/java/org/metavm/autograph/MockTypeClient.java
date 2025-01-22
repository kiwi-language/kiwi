package org.metavm.autograph;

import org.metavm.object.type.TypeManager;
import org.metavm.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class MockTypeClient implements TypeClient {

    public static final Logger logger = LoggerFactory.getLogger(MockTypeClient.class);

    private final TypeManager typeManager;
    private final ExecutorService executor;
    private final TransactionOperations transactionOperations;

    public MockTypeClient(TypeManager typeManager,
                          ExecutorService executor,
                          TransactionOperations transactionOperations) {
        this.typeManager = typeManager;
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
    public void deploy(String mvaFile) {
        submit(() -> transactionOperations.execute(status -> {
            try(var input = new FileInputStream(mvaFile)) {
                typeManager.deploy(input);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }), "deploy");
    }

    @Override
    public void login(long appId, String loginName, String password) {
        submit(() -> ContextUtil.setAppId(appId));
    }

    @Override
    public boolean ping() {
        return submit(() -> true, "ping");
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

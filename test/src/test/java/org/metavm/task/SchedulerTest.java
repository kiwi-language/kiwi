package org.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.MemInstanceStore;
import org.metavm.mocks.TestTask;
import org.metavm.util.*;
import org.springframework.transaction.support.TransactionOperations;

import static org.metavm.util.TestConstants.APP_ID;

public class SchedulerTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private Scheduler scheduler;
    private Worker worker;
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceStore = bootResult.instanceStore();
        entityContextFactory = bootResult.entityContextFactory();
        TransactionOperations transactionOperations = new MockTransactionOperations();
        scheduler = new Scheduler(entityContextFactory, transactionOperations);
        worker = new Worker(entityContextFactory, transactionOperations, new DirectTaskRunner());
        ContextUtil.setAppId(APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        scheduler = null;
        worker = null;
        instanceStore = null;
    }

    public void test() {
        var ref = new Object() {
            TestTask task;
        };
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                ref.task = new TestTask();
                context.bind(ref.task);
                context.finish();
            }
        });
        Assert.assertNotNull(instanceStore.get(TestConstants.APP_ID, ref.task.getId().getTreeId()));
        TestUtils.doInTransactionWithoutResult(() -> scheduler.sendHeartbeat());
        scheduler.sendHeartbeat();
        Assert.assertTrue(scheduler.isActive());
        try(var context = newPlatformContext()) {
            var registry = SchedulerRegistry.getInstance(context);
            Assert.assertEquals(NetworkUtils.localIP, registry.getIp());
        }
        worker.sendHeartbeat();
        try(var context = newPlatformContext()) {
            var executorData = context.selectFirstByKey(ExecutorData.IDX_IP, NetworkUtils.localIP);
            Assert.assertNotNull("Executor not registered", executorData);
            Assert.assertTrue(executorData.isAvailable());
        }
        scheduler.schedule();
        try(var context = newPlatformContext()) {
            var tasks = context.selectByKey(ShadowTask.IDX_EXECUTOR_IP, NetworkUtils.localIP);
            Assert.assertEquals(1, tasks.size());
        }
        Assert.assertTrue(worker.waitFor(t -> t.idEquals(ref.task.getId()), 10));
        Assert.assertNull(instanceStore.get(TestConstants.APP_ID, ref.task.getId().getTreeId()));
    }

    private IEntityContext newPlatformContext() {
        return entityContextFactory.newContext(Constants.PLATFORM_APP_ID);
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(APP_ID);
    }

}
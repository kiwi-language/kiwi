package org.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.MemInstanceStore;
import org.metavm.entity.MetaContextCache;
import org.metavm.mocks.TestTask;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.*;
import org.springframework.transaction.support.TransactionOperations;

import static org.metavm.util.TestConstants.APP_ID;

public class SchedulerTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private Scheduler scheduler;
    private Worker worker;
    private MetaContextCache metaContextCache;
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceStore = new MemInstanceStore(bootResult.mapperRegistry());
        entityContextFactory = bootResult.entityContextFactory();
        metaContextCache = bootResult.metaContextCache();
        TransactionOperations transactionOperations = new MockTransactionOperations();
        scheduler = new Scheduler(entityContextFactory, transactionOperations);
        worker = new Worker(entityContextFactory, transactionOperations, new DirectTaskRunner(), metaContextCache);
        ContextUtil.setAppId(APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        scheduler = null;
        worker = null;
        metaContextCache = null;
        instanceStore = null;
    }

    public void test() {
        TestUtils.waitForAllTasksDone(new SchedulerAndWorker(scheduler, worker, metaContextCache, entityContextFactory));
        var ref = new Object() {
            TestTask task;
        };
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                ref.task = new TestTask(context.allocateRootId());
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
            var executorData = context.selectFirstByKey(ExecutorData.IDX_IP,
                    Instances.stringInstance(NetworkUtils.localIP));
            Assert.assertNotNull("Executor not registered", executorData);
            Assert.assertTrue(executorData.isAvailable());
        }
        scheduler.schedule();
        try(var context = newPlatformContext()) {
            var tasks = context.selectByKey(ShadowTask.IDX_EXECUTOR_IP_START_AT,
                    Instances.stringInstance(NetworkUtils.localIP),
                    Instances.longInstance(0L));
            Assert.assertEquals(1, tasks.size());
        }
        Assert.assertTrue(worker.waitFor(t -> t.idEquals(ref.task.getId()), 10, 0));
    }

    private IInstanceContext newPlatformContext() {
        return entityContextFactory.newContext(Constants.PLATFORM_APP_ID);
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(APP_ID);
    }

}
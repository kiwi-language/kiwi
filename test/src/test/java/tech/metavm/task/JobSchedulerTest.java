package tech.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.TestJob;
import tech.metavm.util.*;

import static tech.metavm.util.TestConstants.APP_ID;

public class JobSchedulerTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private Scheduler jobScheduler;
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceStore = bootResult.instanceStore();
        entityContextFactory = bootResult.entityContextFactory();
        TransactionOperations transactionOperations = new MockTransactionOperations();
        Scheduler.THREAD_POOL_SIZE = 1;
        jobScheduler = new Scheduler(entityContextFactory, transactionOperations);
        ContextUtil.setAppId(TestConstants.getAppId());
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        jobScheduler = null;
        instanceStore = null;
    }

    public void test() {
        var ref = new Object() {
            TestJob testJob;
        };
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                ref.testJob = new TestJob();
                context.bind(ref.testJob);
                context.finish();
            }
        });
        Assert.assertNotNull(instanceStore.get(TestConstants.APP_ID, ref.testJob.getId().getPhysicalId()));
        TestUtils.doInTransactionWithoutResult(() -> jobScheduler.sendHeartbeat());
        jobScheduler.pollSignals();
        jobScheduler.schedule();
        jobScheduler.waitForJobDone(ref.testJob, 20);
        Assert.assertNull(instanceStore.get(TestConstants.APP_ID, ref.testJob.getId().getPhysicalId()));
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(Constants.getAppId(APP_ID));
    }

}
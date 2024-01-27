package tech.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.mocks.TestJob;
import tech.metavm.object.instance.MockInstanceLogService;
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
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    public void test() {
        TestJob testJob;
        TestUtils.beginTransaction();
        try(var context = newContext()) {
            testJob = new TestJob();
            context.bind(testJob);
            context.finish();
        }
        TestUtils.commitTransaction();
        Assert.assertNotNull(instanceStore.get(testJob.getId()));
        TestUtils.doInTransactionWithoutResult(() -> jobScheduler.sendHeartbeat());
        jobScheduler.pollSignals();
        jobScheduler.schedule();
        jobScheduler.waitForJobDone(testJob, 10);
        Assert.assertNull(instanceStore.get(testJob.getId()));
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(APP_ID);
    }

}
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
    @SuppressWarnings("FieldCanBeLocal")
    private TransactionOperations transactionOperations;
    @SuppressWarnings("FieldCanBeLocal")
    private MemInstanceStore instanceStore;
    private Scheduler jobScheduler;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        entityContextFactory = TestUtils.getEntityContextFactory(idProvider, instanceStore, new MockInstanceLogService(), instanceStore.getIndexEntryMapper());
        transactionOperations = new MockTransactionOperations();
        Scheduler.THREAD_POOL_SIZE = 1;
        jobScheduler = new Scheduler(entityContextFactory, transactionOperations);
        jobScheduler.createSchedulerStatus();
    }

    public void test() {
        TestJob testJob;
        try(IEntityContext context = newContext()) {
            testJob = new TestJob();
            context.bind(testJob);
            context.finish();

            jobScheduler.sendHeartbeat();
            jobScheduler.pollSignals();
            jobScheduler.waitForJobDone(testJob, 10);
        }

        try(var context = newContext()) {
            testJob = context.getEntity(TestJob.class, testJob.getIdRequired());
            Assert.assertTrue(testJob.isFinished());
            Assert.assertEquals(10, testJob.getCount());
        }
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(APP_ID);
    }

}
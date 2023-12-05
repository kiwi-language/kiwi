package tech.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.TestJob;
import tech.metavm.util.*;

import static tech.metavm.util.TestConstants.APP_ID;

public class JobSchedulerTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;
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
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        transactionOperations = new MockTransactionOperations();
        Scheduler.THREAD_POOL_SIZE = 1;
        jobScheduler = new Scheduler(instanceContextFactory, transactionOperations);
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
        return instanceContextFactory.newEntityContext(APP_ID);
    }

}
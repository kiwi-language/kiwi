package tech.metavm.job;

import junit.framework.TestCase;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.TestJob;
import tech.metavm.util.*;

public class JobSchedulerTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;
    @SuppressWarnings("FieldCanBeLocal")
    private TransactionOperations transactionOperations;
    @SuppressWarnings("FieldCanBeLocal")
    private MemInstanceStore instanceStore;
    private JobScheduler jobScheduler;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        transactionOperations = new MockTransactionOperations();
        JobScheduler.THREAD_POOL_SIZE = 1;
        jobScheduler = new JobScheduler(instanceContextFactory, transactionOperations);
        jobScheduler.createSchedulerStatus();
    }

    public void test() {
        IEntityContext context = newContext();
        TestJob testJob = new TestJob();
        JobSignal jobSignal = new JobSignal(TestConstants.TENANT_ID);
        jobSignal.setLastJobCreatedAt(System.currentTimeMillis());
        jobSignal.setUnfinishedCount(1);
        context.bind(jobSignal);
        context.bind(testJob);
        context.finish();

        jobScheduler.sendHeartbeat();
        jobScheduler.pollSignals();
        jobScheduler.waitForJobDone(testJob, 10);

        context = newContext();
        testJob = context.getEntity(TestJob.class, testJob.getId());
        Assert.assertTrue(testJob.isFinished());
        Assert.assertEquals(10, testJob.getCount());
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newContext(TestConstants.TENANT_ID).getEntityContext();
    }

    private IEntityContext newRootContext() {
        return instanceContextFactory.newRootContext().getEntityContext();
    }

}
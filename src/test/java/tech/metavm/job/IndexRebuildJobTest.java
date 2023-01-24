package tech.metavm.job;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TestUtils;

import java.util.List;

public class IndexRebuildJobTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;

    private MemInstanceSearchService instanceSearchService;

    @Override
    protected void setUp() throws Exception {
        MemInstanceStore instanceStore = new MemInstanceStore();
        MockIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceSearchService = new MemInstanceSearchService();
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore, instanceSearchService);

    }

    public void test() {
        IndexRebuildJob job = new IndexRebuildJob();
        {
            IInstanceContext context = newContext();
            for (int i = 0; i < 100; i++) {
                context.bind(MockRegistry.getNewFooInstance());
            }

            context.getEntityContext().bind(job);
            context.getEntityContext().finish();
        }

        instanceSearchService.clear();

        {
            IInstanceContext context1 = newContext();
            job = context1.getEntityContext().getEntity(IndexRebuildJob.class, job.getId());
            for (int i = 0; i < 50; i++) {
                if (job.run0(context1)) {
                    break;
                }
            }
            context1.finish();
        }

        {
            IInstanceContext context = newContext();
            ClassType fooType = MockRegistry.getClassType(Foo.class);
            List<Instance> instances = context.getByType(fooType, null, 100);
            for (Instance instance : instances) {
                Assert.assertTrue(instanceSearchService.contains(instance.getId()));
            }
        }


    }

    private IInstanceContext newContext() {
        return instanceContextFactory.newContext(TestConstants.TENANT_ID);
    }

}
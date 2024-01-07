package tech.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TestUtils;

public class IndexRebuildJobTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    private MemInstanceSearchService instanceSearchService;

    @Override
    protected void setUp() throws Exception {
        MemInstanceStore instanceStore = new MemInstanceStore();
        MockIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceSearchService = new MemInstanceSearchService();
        entityContextFactory = TestUtils.getEntityContextFactory(idProvider, instanceStore, new MockInstanceLogService(), new MemIndexEntryMapper());
    }

    public void test() {
        IndexRebuildTask job = new IndexRebuildTask();
        try(var context = newContext()){
            for (int i = 0; i < 100; i++) {
                context.bind(MockRegistry.getNewFooInstance());
            }
            context.bind(job);
            context.finish();
        }

        instanceSearchService.clear();

        try (var context1 = newContext()) {
            job = context1.getEntity(IndexRebuildTask.class, job.tryGetId());
            for (int i = 0; i < 50; i++) {
                if (job.run0(context1)) {
                    break;
                }
            }
            context1.finish();
        }

        try (var context = newContext()) {
            var instances = context.getByType(Foo.class, null, 100);
            for (var instance : instances) {
                Assert.assertTrue(instanceSearchService.contains(instance.tryGetId()));
            }
        }

    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
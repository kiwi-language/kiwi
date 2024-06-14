package org.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.ContextUtil;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

public class IndexRebuildJobTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private MemInstanceSearchServiceV2 instanceSearchService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceSearchService = bootResult.instanceSearchService();
        entityContextFactory = bootResult.entityContextFactory();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        instanceSearchService = null;
    }

    public void test() {
        IndexRebuildTask job = new IndexRebuildTask();
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                for (int i = 0; i < 100; i++) {
                    context.bind(new Foo("foo" + i, new Bar("bar" + i)));
                }
                context.bind(job);
                context.finish();
            }
        });
        instanceSearchService.clear();
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var job2 = context.getEntity(IndexRebuildTask.class, job.getId());
                for (int i = 0; i < 50; i++) {
                    if (job2.run0(context))
                        break;
                }
                context.finish();
            }
        });
        try (var context = newContext()) {
            var instances = context.selectByKey(Foo.IDX_ALL_FLAG, true);
            for (var instance : instances) {
                Assert.assertTrue(instanceSearchService.contains(instance.getId()));
            }
        }

    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
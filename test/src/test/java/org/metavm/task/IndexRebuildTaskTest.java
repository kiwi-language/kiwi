package org.metavm.task;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.*;

public class IndexRebuildTaskTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private MemInstanceSearchServiceV2 instanceSearchService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceSearchService = bootResult.instanceSearchService();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        instanceSearchService = null;
        schedulerAndWorker = null;
    }

    public void test() {
        var task = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                for (int i = 0; i < 100; i++) {
                    var foo = new Foo(context.allocateRootId(), "foo" + i, null);
                    foo.setBar(new Bar(foo.nextChildId(), foo, "bar" + i));
                    context.bind(foo);
                }
                var t = new IndexRebuildTask(context.allocateRootId());
                context.bind(t);
                context.finish();
                return t;
            }
        });
        instanceSearchService.clear();
        instanceSearchService.createSystemIndices();
        instanceSearchService.createIndex(TestConstants.APP_ID, false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var job2 = context.getEntity(IndexRebuildTask.class, task.getId());
                for (int i = 0; i < 50; i++) {
                    if (job2.run0(context, context))
                        break;
                }
                context.finish();
            }
        });
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        try (var context = newContext()) {
            var instances = context.selectByKey(Foo.IDX_ALL_FLAG, Instances.trueInstance());
            for (var instance : instances) {
                Assert.assertTrue(instanceSearchService.contains(instance.getTreeId()));
            }
        }

    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
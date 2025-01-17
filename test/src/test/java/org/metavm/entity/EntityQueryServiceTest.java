package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.common.Page;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class EntityQueryServiceTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(EntityQueryServiceTest.class);

    private EntityQueryService entityQueryService;
    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var instanceSearchService = bootResult.instanceSearchService();
        entityContextFactory = bootResult.entityContextFactory();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityQueryService = new EntityQueryService(instanceSearchService);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        entityContextFactory = null;
        entityQueryService = null;
        schedulerAndWorker = null;
    }

    public <T extends Entity> T addEntity(T entity) {
//        if (!entityContext.containsModel(entity)) {
//            entityContext.bind(entity);
//            entityContext.initIds();
//        }
//        instanceSearchService.add(getAppId(), (ClassInstance) entityContext.getInstance(entity));
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                context.bind(entity);
                context.finish();
            }
        });
        return entity;
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

    public void test() {
        Foo foo = addEntity(MockUtils.getFoo());
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            var qux = context.getEntity(Qux.class, Objects.requireNonNull(foo.getQux()).getId());
            Page<Foo> page = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Foo.class)
                            .addField(Foo.esName, Instances.stringInstance(foo.getName()))
                            .addField(Foo.esQux, qux.getReference())
                            .build(),
                    context
            );
            Assert.assertEquals(1, page.total());
            Assert.assertSame(foo, page.data().getFirst());
        }
    }

    public void testSearchText() {
        Foo foo = addEntity(MockUtils.getFoo());
        logger.info("Foo ID: {}", foo.getId());
        DebugEnv.id = foo.getId();
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            Page<Foo> page = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Foo.class)
                            .addField(Foo.esCode, Instances.stringInstance("Foo001"))
                            .build(),
                    context
            );
            Assert.assertEquals(1, page.total());
            Assert.assertSame(foo, page.data().getFirst());
        }
    }

}
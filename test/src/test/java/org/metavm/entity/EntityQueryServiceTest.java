package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.application.Application;
import org.metavm.common.Page;
import org.metavm.compiler.util.List;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.user.PlatformUser;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public <T extends Entity> T addEntity(Function<Supplier<Id>, T> creator) {
        return TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var entity = creator.apply(context::allocateRootId);
                context.bind(entity);
                context.finish();
                return entity;
            }
        });
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

    public void test() {
        Foo foo = addEntity(MockUtils.getFooCreator());
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
            Assert.assertSame(foo, page.items().getFirst());
        }
    }

    public void testSearchText() {
        Foo foo = addEntity(MockUtils.getFooCreator());
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
            Assert.assertSame(foo, page.items().getFirst());
        }
    }

    public void testPagesize() {
        var appId = TestUtils.doInTransaction(() -> {
            try (var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                var user = new PlatformUser(context.allocateRootId(), "test", "123456", "test", List.of());
                context.bind(user);
                context.bind(new Application(
                        context.allocateRootId(),
                        "app1",
                        user
                ));

                context.bind(new Application(
                        context.allocateRootId(),
                        "app2",
                        user
                ));

                Application app;
                context.bind(app = new Application(
                        context.allocateRootId(),
                        "app3",
                        user
                ));
                context.finish();
                return app.getId();
            }
        });
        TestUtils.waitForEsSync(schedulerAndWorker);

        try (var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
            ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
            var app = (Application) context.get(appId);
            assertEquals("app3", app.getName());
            var page = entityQueryService.query(EntityQueryBuilder.newBuilder(Application.class)
                    .page(1)
                    .pageSize(2)
                    .build(),
                    context
            );
            assertEquals(2, page.items().size());
            logger.debug("First app name: {}", page.items().getFirst().getName());
        }
    }


}
package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.Page;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.type.Klass;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;

public class EntityQueryServiceTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(EntityQueryServiceTest.class);

    private EntityQueryService entityQueryService;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var instanceSearchService = bootResult.instanceSearchService();
        entityContextFactory = bootResult.entityContextFactory();
        var instanceQueryService = new InstanceQueryService(instanceSearchService);
        entityQueryService = new EntityQueryService(instanceQueryService);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        entityContextFactory = null;
        entityQueryService = null;
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

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

    public void test() {
        Foo foo = addEntity(MockUtils.getFoo());
        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            var qux = context.getEntity(Qux.class, Objects.requireNonNull(foo.getQux()).getId());
            Page<Foo> page = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Foo.class)
                            .addField("name", foo.getName())
                            .addField("qux", qux)
                            .build(),
                    context
            );
            Assert.assertEquals(1, page.total());
            Assert.assertSame(foo, page.data().get(0));
        }
    }

    public void testSearchText() {
        Foo foo = addEntity(MockUtils.getFoo());
        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            Page<Foo> page = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Foo.class)
                            .searchText("Foo001")
                            .searchFields(List.of("code"))
                            .build(),
                    context
            );
            Assert.assertEquals(1, page.total());
            Assert.assertSame(foo, page.data().get(0));
        }
    }

    public void testSearchTypes() {
        Klass fooType = ModelDefRegistry.getClassType(Foo.class).resolve();
        try (var context = entityContextFactory.newContext(Constants.ROOT_APP_ID)) {
            Page<Klass> page = entityQueryService.query(
                    EntityQueryBuilder.newBuilder(Klass.class)
                            .addField("kind", fooType.getKind())
                            .addField("name", fooType.getName())
                            .includeBuiltin(true)
                            .build(),
                    context
            );
            Assert.assertEquals(1, page.total());
            Assert.assertSame(fooType, page.data().get(0));
        }
    }


}
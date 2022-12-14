package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.dto.Page;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.List;

import static tech.metavm.util.TestContext.getTenantId;

public class EntityQueryServiceTest extends TestCase {

    private MockEntityContext entityContext;
    private MemInstanceSearchService instanceSearchService;
    private EntityQueryService entityQueryService;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        entityContext = new MockEntityContext(MockRegistry.getDefContext(), idProvider, MockRegistry.getDefContext());
        instanceSearchService = new MemInstanceSearchService();
        InstanceQueryService instanceQueryService = new InstanceQueryService(instanceSearchService);
        entityQueryService = new EntityQueryService(instanceQueryService);
    }

    public <T extends Entity> T addEntity(T entity) {
        if(!entityContext.containsModel(entity)) {
            entityContext.bind(entity);
            entityContext.initIds();
        }
        instanceSearchService.add(getTenantId(), entityContext.getEntityInstance(entity));
        return entity;
    }

    public void test() {
        Foo foo = addEntity(MockRegistry.getFoo());
        Page<Foo> page = entityQueryService.query(
                new EntityQuery<>(
                        Foo.class,
                        null,
                        List.of(),
                        1,
                        20,
                        List.of(
                                new EntityQueryField("name", foo.getName()),
                                new EntityQueryField("qux", foo.getQux())
                        )
                ),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(foo, page.data().get(0));
    }

    public void testSearchText() {
        Foo foo = addEntity(MockRegistry.getFoo());
        Page<Foo> page = entityQueryService.query(
                new EntityQuery<>(
                        Foo.class,
                        "Foo001",
                        List.of("code"),
                        1,
                        20,
                        List.of()
                ),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(foo, page.data().get(0));
    }

    public void testSearchTypes() {
        ClassType fooType = addEntity(MockRegistry.getClassType(Foo.class));
        Page<ClassType> page = entityQueryService.query(
                new EntityQuery<>(
                        ClassType.class,
                        null,
                        List.of(),
                        1,
                        20,
                        List.of(
                                new EntityQueryField("category", fooType.getCategory()),
                                new EntityQueryField("name", fooType.getName())
                        )
                ),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(fooType, page.data().get(0));
    }

}
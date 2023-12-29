package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.Page;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestConstants;

import java.util.List;

import static tech.metavm.util.TestContext.getAppId;

public class EntityQueryServiceTest extends TestCase {

    private MemInstanceSearchService instanceSearchService;
    private EntityQueryService entityQueryService;
    private IEntityContext entityContext;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        instanceSearchService = new MemInstanceSearchService();
        InstanceQueryService instanceQueryService = new InstanceQueryService(instanceSearchService);
        entityQueryService = new EntityQueryService(instanceQueryService);
        entityContext = MockRegistry.newEntityContext(TestConstants.APP_ID);
    }

    public <T extends Entity> T addEntity(T entity) {
        if (!entityContext.containsModel(entity)) {
            entityContext.bind(entity);
            entityContext.initIds();
        }
        instanceSearchService.add(getAppId(), (ClassInstance) entityContext.getInstance(entity));
        return entity;
    }

    public void test() {
        Foo foo = addEntity(MockRegistry.getFoo());
        Page<Foo> page = entityQueryService.query(
                EntityQueryBuilder.newBuilder(Foo.class)
                        .addField("name", foo.getName())
                        .addField("qux", foo.getQux())
                        .build(),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(foo, page.data().get(0));
    }

    public void testSearchText() {
        Foo foo = addEntity(MockRegistry.getFoo());
        Page<Foo> page = entityQueryService.query(
                EntityQueryBuilder.newBuilder(Foo.class)
                        .searchText("Foo001")
                        .searchFields(List.of("code"))
                        .build(),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(foo, page.data().get(0));
    }

    public void testSearchTypes() {
        ClassType fooType = addEntity(MockRegistry.getClassType(Foo.class));
        Page<ClassType> page = entityQueryService.query(
                EntityQueryBuilder.newBuilder(ClassType.class)
                        .addField("category", fooType.getCategory())
                        .addField("name", fooType.getName())
                        .build(),
                entityContext
        );
        Assert.assertEquals(1, page.total());
        Assert.assertSame(fooType, page.data().get(0));
    }

}
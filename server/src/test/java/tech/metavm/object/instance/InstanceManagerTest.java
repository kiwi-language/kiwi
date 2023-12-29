package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.LoadInstancesByPathsRequest;
import tech.metavm.object.instance.rest.SelectRequest;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.Constants;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

import java.util.List;

public class InstanceManagerTest extends TestCase {

    private InstanceManager instanceManager;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        var instanceStore = new MemInstanceStore();
        var instanceSearchService = new MemInstanceSearchService();
        var instanceQueryService = new InstanceQueryService(instanceSearchService);
        var instanceLogService = new MockInstanceLogService();
        var indexEntryMapper = new MemIndexEntryMapper();
        entityContextFactory = TestUtils.getEntityContextFactory(
                idProvider, instanceStore, instanceLogService, indexEntryMapper
        );
        instanceManager = new InstanceManager(entityContextFactory, instanceStore, instanceQueryService);
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext();
    }

    private Foo saveFoo(IEntityContext context) {
        var foo = new Foo("Big Foo", new Bar("Bar001"));
        foo.setBazList(List.of(
                new Baz(
                        List.of(
                                new Bar("Bar002"),
                                new Bar("Bar003")
                        )
                ),
                new Baz(
                        List.of(
                                new Bar("Bar004"),
                                new Bar("Bar005")
                        )
                )
        ));
        foo.setQux(new Qux(100));

        context.bind(foo);
        context.finish();
        return foo;
    }

    public void testLoadByPaths() {
        var context = newContext();
        var foo = saveFoo(context);
        var result = instanceManager.loadByPaths(
                new LoadInstancesByPathsRequest(
                        null,
                        List.of(
                                Constants.CONSTANT_ID_PREFIX + foo.getId() + ".巴",
                                Constants.CONSTANT_ID_PREFIX + foo.getId() + ".巴.编号",
                                Constants.CONSTANT_ID_PREFIX + foo.getId() + ".巴子.*.巴巴巴巴.0.编号",
                                Constants.CONSTANT_ID_PREFIX + foo.getId() + ".巴子.*.巴巴巴巴.1.编号"
                        )
                )
        );
        Assert.assertEquals(
                List.of(
                        context.getInstance(foo.getBar()).toDTO(),
                        MockRegistry.createString("Bar001").toDTO(),
                        MockRegistry.createString("Bar002").toDTO(),
                        MockRegistry.createString("Bar004").toDTO(),
                        MockRegistry.createString("Bar003").toDTO(),
                        MockRegistry.createString("Bar005").toDTO()
                ),
                result
        );
    }

    public void testSelect() {
        var context = newContext();
        var foo = saveFoo(context);
        var fooType = MockRegistry.getClassType(Foo.class);
        var page = instanceManager.select(new SelectRequest(
                fooType.getIdRequired(),
                List.of(
                        "巴.编号",
                        "量子X"
                ),
                "名称 = 'Big Foo'",
                1,
                20
        ));
        Assert.assertEquals(1, page.total());
        Assert.assertEquals(
                List.of(
                        MockRegistry.createString("Bar001").toDTO(),
                        context.getInstance(foo.getQux()).toDTO()
                ),
                List.of(
                    page.data().get(0)[0],
                    page.data().get(0)[1]
                )
        );
    }

}
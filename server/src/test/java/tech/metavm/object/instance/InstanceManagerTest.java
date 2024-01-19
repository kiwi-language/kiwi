package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.rest.LoadInstancesByPathsRequest;
import tech.metavm.object.instance.rest.SelectRequest;
import tech.metavm.util.*;

import java.util.List;

public class InstanceManagerTest extends TestCase {

    private InstanceManager instanceManager;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        var instanceStore = new MemInstanceStore();
        var instanceSearchService = new MemInstanceSearchService();
        var instanceQueryService = new InstanceQueryService(instanceSearchService);
        var indexEntryMapper = new MemIndexEntryMapper();
        var instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
        var instanceLogService = new InstanceLogServiceImpl(
                entityContextFactory, instanceSearchService, instanceStore, List.of());
        entityContextFactory.setInstanceLogService(instanceLogService);
        BootstrapUtils.bootstrap(entityContextFactory);
        instanceManager = new InstanceManager(entityContextFactory, instanceStore, instanceQueryService);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(false);
    }

    private Foo saveFoo() {
        TestUtils.startTransaction();
        try (var context = newContext()) {
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
            TestUtils.commitTransaction();
            return foo;
        }
    }

    public void testLoadByPaths() {
        var context = newContext();
        var foo = saveFoo();
        var result = instanceManager.loadByPaths(
                new LoadInstancesByPathsRequest(
                        null,
                        List.of(
                                Constants.CONSTANT_ID_PREFIX + foo.tryGetId() + ".巴",
                                Constants.CONSTANT_ID_PREFIX + foo.tryGetId() + ".巴.编号",
                                Constants.CONSTANT_ID_PREFIX + foo.tryGetId() + ".巴子.*.巴巴巴巴.0.编号",
                                Constants.CONSTANT_ID_PREFIX + foo.tryGetId() + ".巴子.*.巴巴巴巴.1.编号"
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
        var foo = saveFoo();
        var fooType = ModelDefRegistry.getClassType(Foo.class);
        var page = instanceManager.select(new SelectRequest(
                fooType.getId(),
                List.of(
                        "巴.编号",
                        "量子X"
                ),
                "名称 = 'Big Foo'",
                1,
                20
        ));
        Assert.assertEquals(1, page.total());
        try (var context = newContext()) {
            Assert.assertEquals(
                    List.of(
                            Instances.createString("Bar001").toDTO(),
                            context.getInstance(foo.getQux()).toDTO()
                    ),
                    List.of(
                            page.data().get(0)[0],
                            page.data().get(0)[1]
                    )
            );
        }
    }

    public void testShopping() {
        var shoppingTypes = MockUtils.createShoppingTypes();
        var shoppingInstances = MockUtils.createShoppingInstances(shoppingTypes);
        Assert.assertNotNull(shoppingInstances.shoesProduct());
    }

}
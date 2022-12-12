package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.util.List;
import java.util.concurrent.Executors;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;

public class BootstrapTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapTest.class);

    private MemInstanceStore instanceStore;
    private MockIdProvider mockIdProvider;
    private MemAllocatorStore allocatorStore;
    private InstanceContextFactory instanceContextFactory;
    private MemInstanceSearchService instanceSearchService;

    @Override
    protected void setUp() {
        ModelDefRegistry.setDefContext(null);
        TestContext.setTenantId(ROOT_TENANT_ID);
        mockIdProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        allocatorStore = new MemAllocatorStore();
        instanceSearchService = new MemInstanceSearchService();

        instanceContextFactory = new InstanceContextFactory(instanceStore);
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService,
                instanceContextFactory,
                instanceStore
        );
        instanceContextFactory.setPlugins(List.of(new ChangeLogPlugin(instanceLogService)));
    }

    @Override
    protected void tearDown() {
        TestContext.resetTenantId();
    }

    public void testSmoking() {
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap.bootAndSave();

        InstanceContext context = new InstanceContext(
                ROOT_TENANT_ID,
                instanceStore,
                mockIdProvider,
                Executors.newSingleThreadExecutor(),
                true,
                List.of(),
                InstanceContextFactory.getStdContext(),
                (txt, typeId) -> txt.getEntityContext().getType(typeId)
        );

        ClassType typeType = ModelDefRegistry.getClassType(ClassType.class);
        Assert.assertNotNull(typeType.getId());
        Assert.assertTrue(instanceSearchService.contains(typeType.getId()));

        IEntityContext entityContext = context.getEntityContext();
        Foo foo = new Foo("大傻", new Bar("巴巴巴巴"));
        entityContext.bind(foo);

        ClassType testType = TypeUtil.createValue("Test Type", null);

        Field titleField = new Field(
                "title", testType, Access.GLOBAL, false, true, null,
                StandardTypes.getStringType(), false
        );

        entityContext.bind(testType);
        entityContext.bind(titleField);

        entityContext.finish();
    }

    public void testRepeatBoot() {
        AllocatorStore allocatorStore = this.allocatorStore;
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap.bootAndSave();
        bootstrap.bootAndSave();
        Bootstrap bootstrap2 = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap2.bootAndSave();
    }

    public void testReboot() {
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap.bootAndSave();

        Bootstrap bootstrap2 = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap2.boot();

        Type typeType = ModelDefRegistry.getType(Type.class);
        Assert.assertNotNull(typeType.getId());

        Type tableType = ModelDefRegistry.getType(Table.class);
        Assert.assertNotNull(tableType.getId());
        Assert.assertTrue(tableType instanceof ArrayType);
        Assert.assertEquals(tableType, StandardTypes.getArrayType());
    }

}
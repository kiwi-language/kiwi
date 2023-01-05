package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.FlowRT;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
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
        Type longType = ModelDefRegistry.getType(Long.class);
        Assert.assertNotNull(typeType.getId());
        Assert.assertTrue(instanceSearchService.contains(typeType.getId()));
        Assert.assertTrue(instanceSearchService.contains(longType.getId()));

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
        DefContext defContext1 = ModelDefRegistry.getDefContext();

//        ClassType typeType1 = ModelDefRegistry.getClassType(Type.class);
//        Table<FlowRT> flows1 = typeType1.getDeclaredFlows();
//        Table<ConstraintRT<?>> constraints1 = typeType1.getDeclaredConstraints();
//        Table<Field> fields1 = typeType1.getDeclaredFields();
//        Assert.assertNotNull(typeType1.getNullableType());

        Bootstrap bootstrap2 = new Bootstrap(instanceContextFactory, new StdAllocators(allocatorStore));
        bootstrap2.boot();

        DefContext defContext2 = ModelDefRegistry.getDefContext();

        for (Class<?> modelClass : ReflectUtils.getModelClasses()) {
            Type type1 = defContext1.getType(modelClass),
                    type2 = defContext2.getType(modelClass);
            Assert.assertEquals(
                    "Identity for class '" + modelClass.getName() + "' changed after reboot",
                    type1.getId(), type2.getId());
        }

        for (Column column : SQLType.columns()) {
            if(defContext2.containsModel(column)) {
                Instance colInstance = defContext2.getInstance(column);
                Assert.assertNotNull(colInstance.getId());
            }
        }

//        ClassType typeType2 = ModelDefRegistry.getClassType(Type.class);

//        Table<FlowRT> flows2 = typeType2.getDeclaredFlows();
//        Table<ConstraintRT<?>> constraints2 = typeType2.getDeclaredConstraints();
//        Table<Field> fields2 = typeType2.getDeclaredFields();

//        Type nullableClassType = ModelDefRegistry.getType(ClassType.class).getNullableType();
//        Assert.assertNotNull(nullableClassType);
//        Assert.assertNotNull(nullableClassType.getId());
//
//        Assert.assertEquals(typeType1.getId(), typeType2.getId());
//        Assert.assertEquals(flows1.getId(), flows2.getId());
//        Assert.assertEquals(fields1.getId(), fields2.getId());
//        Assert.assertEquals(constraints1.getId(), constraints2.getId());
//
//        Assert.assertNotNull(typeType2.getNullableType());
//        Assert.assertNotNull(typeType2.getNullableType().getId());
//        Assert.assertNotNull(typeType2.getArrayType());
//        Assert.assertNotNull(typeType2.getArrayType().getId());
//
//        Type tableType = ModelDefRegistry.getType(Table.class);
//        Assert.assertNotNull(tableType.getId());
//        Assert.assertTrue(tableType instanceof ArrayType);
//        Assert.assertEquals(tableType, StandardTypes.getArrayType());
//
//        Instance enumConstantInst = ModelDefRegistry.getDefContext().getInstance(TypeCategory.CLASS);
//        Assert.assertNotNull(enumConstantInst.getId());
//
//        Type nullableClassType2 = ModelDefRegistry.getType(ClassType.class).getNullableType();
//        Assert.assertNotNull(nullableClassType2);
//        Assert.assertEquals(nullableClassType.getId(), nullableClassType2.getId());
    }

}
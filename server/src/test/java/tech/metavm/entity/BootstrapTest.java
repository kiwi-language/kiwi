package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.event.MockEventQueue;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.type.*;
import tech.metavm.util.Column;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.TestContext;

import java.util.List;

import static tech.metavm.util.Constants.ROOT_APP_ID;

public class BootstrapTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapTest.class);

    private MemInstanceStore instanceStore;
    private MockIdProvider mockIdProvider;
    private MemAllocatorStore allocatorStore;
    private InstanceContextFactory instanceContextFactory;
    private EntityContextFactory entityContextFactory;
    private MemInstanceSearchService instanceSearchService;

    @Override
    protected void setUp() {
        ModelDefRegistry.setDefContext(null);
        TestContext.setAppId(ROOT_APP_ID);
        mockIdProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        allocatorStore = new MemAllocatorStore();
        instanceSearchService = new MemInstanceSearchService();

        instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue());
        entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                entityContextFactory,
                instanceSearchService,
                instanceStore,
                List.of());
        instanceContextFactory.setIdService(mockIdProvider);
        entityContextFactory.setInstanceLogService(instanceLogService);
    }

    @Override
    protected void tearDown() {
        TestContext.resetAppId();
    }

    public void testSmoking() {
        Bootstrap bootstrap = new Bootstrap(
                entityContextFactory, instanceContextFactory, new StdAllocators(allocatorStore), new MemColumnStore());
        bootstrap.bootAndSave();

        try(var context = entityContextFactory.newContext(ROOT_APP_ID)) {

            ClassType typeType = ModelDefRegistry.getClassType(ClassType.class);
            Type longType = ModelDefRegistry.getType(Long.class);
            Assert.assertNotNull(typeType.getId());
            Assert.assertTrue(instanceSearchService.contains(typeType.getId()));
            Assert.assertTrue(instanceSearchService.contains(longType.getIdRequired()));

            Foo foo = new Foo("大傻", new Bar("巴巴巴巴"));
            context.bind(foo);

            ClassType testType = ClassBuilder.newBuilder("Test Type", null).build();
            Field titleField = FieldBuilder
                    .newBuilder("title", null, testType, StandardTypes.getStringType())
                    .build();
            testType.setTitleField(titleField);
            context.bind(testType);
            context.bind(titleField);

            context.finish();
        }
    }

    public void testRepeatBoot() {
        AllocatorStore allocatorStore = this.allocatorStore;
        Bootstrap bootstrap = new Bootstrap(entityContextFactory, instanceContextFactory, new StdAllocators(allocatorStore), new MemColumnStore());
        bootstrap.bootAndSave();
        bootstrap.bootAndSave();
        Bootstrap bootstrap2 = new Bootstrap(entityContextFactory, instanceContextFactory, new StdAllocators(allocatorStore), new MemColumnStore());
        bootstrap2.bootAndSave();
    }

    public void testReboot() {
        Bootstrap bootstrap = new Bootstrap(entityContextFactory, instanceContextFactory, new StdAllocators(allocatorStore), new MemColumnStore());
        bootstrap.bootAndSave();
        DefContext defContext1 = ModelDefRegistry.getDefContext();

//        ClassType typeType1 = ModelDefRegistry.getClassType(Type.class);
//        Table<FlowRT> flows1 = typeType1.getDeclaredFlows();
//        Table<ConstraintRT<?>> constraints1 = typeType1.getDeclaredConstraints();
//        Table<Field> fields1 = typeType1.getDeclaredFields();
//        Assert.assertNotNull(typeType1.getNullableType());

        Bootstrap bootstrap2 = new Bootstrap(entityContextFactory, instanceContextFactory, new StdAllocators(allocatorStore), new MemColumnStore());
        bootstrap2.boot();

        DefContext defContext2 = ModelDefRegistry.getDefContext();

        for (Class<?> modelClass : EntityUtils.getModelClasses()) {
            Type type1 = defContext1.getType(modelClass),
                    type2 = defContext2.getType(modelClass);
            Assert.assertEquals(
                    "Identity for class '" + modelClass.getName() + "' changed after reboot",
                    type1.getId(), type2.getId());
        }

        for (Column column : ColumnKind.columns()) {
            if(defContext2.containsModel(column)) {
                var colInstance = defContext2.getInstance(column);
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
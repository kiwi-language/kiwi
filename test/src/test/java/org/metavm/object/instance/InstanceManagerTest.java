package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.FlowSavingContext;
import org.metavm.mocks.FooState;
import org.metavm.mocks.IndexFoo;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class InstanceManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceManagerTest.class);

    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private TypeManager typeManager;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
        typeManager = managers.typeManager();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
        ContextUtil.setAppId(TestConstants.APP_ID);
        FlowSavingContext.initConfig();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        schedulerAndWorker = null;
        typeManager = null;
        apiClient = null;
        FlowSavingContext.clearConfig();
    }

    private IInstanceContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

    public void testShopping() {
        var shoppingTypes = MockUtils.createShoppingTypes();
        var shoppingInstances = MockUtils.createShoppingInstances(shoppingTypes);
        Assert.assertNotNull(shoppingInstances.shoesProduct());
    }

    public void testUtils() {
        MockUtils.assemble("kiwi/util/Utils.kiwi", typeManager, schedulerAndWorker);
        var className = "util.Utils";
        var contains = (boolean) callMethod(className, "test", List.of(
                List.of("a", "b", "c"), List.of("d", "b")
        ));
        Assert.assertTrue(contains);

        var contains2 = (boolean) callMethod(className, "test2", List.of(
           List.of("a", "b", "c"), "d", "b")
        );
        Assert.assertTrue(contains2);
    }

    public void testGenericOverloading() {
        MockUtils.assemble("kiwi/GenericOverloading.kiwi", typeManager, schedulerAndWorker);
        var subId = saveInstance("Sub", Map.of());
        var result = (boolean) callMethod(subId, "test<string>", List.of("abc"));
        Assert.assertTrue(result);
    }

    public void testLambda() {
        MockUtils.assemble("kiwi/Lambda.kiwi", typeManager, schedulerAndWorker);
        var result = (int) callMethod("Utils", "findGt", List.of(
                List.of(1, 2, 3), 2
        ));
        Assert.assertEquals(3, result);
    }

    public void testLivingBeing() {
        MockUtils.createLivingBeingTypes(typeManager, schedulerAndWorker);
        var humanId = saveInstance("Human", Map.of(
                "age", 30,
                "intelligence", 180,
                "occupation", "Inventor"
                )
        );
        var human = getObject(humanId);
        Assert.assertEquals(30, human.getInt(("age")));
        Assert.assertNull(human.get("extra"));
        Assert.assertEquals(180, human.getInt("intelligence"));
        Assert.assertEquals("Inventor", human.getString("occupation"));
        Assert.assertFalse(human.getBoolean("thinking"));
        var makeSoundResult = callMethod(humanId, "makeSound", List.of());
        Assert.assertEquals("I am a human being", makeSoundResult);
        callMethod(humanId, "think", List.of());
        var reloadedHuman = getObject(humanId);
        Assert.assertTrue(reloadedHuman.getBoolean("thinking"));
    }

    public void testIndexQuery() {
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var foo = new IndexFoo(context.allocateRootId());
                context.bind(foo);
                context.finish();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var fooByState = context.selectFirstByKey(IndexFoo.IDX_STATE, Instances.intInstance(FooState.STATE1.code()));
                Assert.assertNotNull(fooByState);
            }
        });
    }

    public void testDeletedField() {
        var klassId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var klass = TestUtils.newKlassBuilder("Foo").build();
                FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();
                context.bind(klass);
                context.finish();
                return klass.getId();
            }
        });
        var instId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                context.loadKlasses();
                var klass = context.getKlass(klassId);
                var inst = ClassInstanceBuilder.newBuilder(klass.getType(), context.allocateRootId())
                        .data(Map.of(
                                klass.getFieldByName("name"),
                                Instances.stringInstance("Leen")
                        )).build();
                context.bind(inst);
                context.finish();
                return inst.getId();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var klass = context.getKlass(klassId);
                var nameField = klass.getFieldByName("name");
                nameField.setMetadataRemoved();
                context.finish();
            }
        });
        var instId2 = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                context.loadKlasses();
                var klass = context.getKlass(klassId);
                var nameField = klass.getFieldByName("name");
                Assert.assertTrue(nameField.isMetadataRemoved());
                var inst = (ClassInstance) context.get(instId);
                Assert.assertTrue(inst.isFieldInitialized(nameField));
                inst.setField(nameField, Instances.stringInstance("Leen2"));
                var inst2 = ClassInstanceBuilder.newBuilder(klass.getType(), context.allocateRootId())
                        .data(Map.of())
                        .build();
                context.bind(inst2);
                context.finish();
                return inst2.getId();
            }
        });
        try(var context = newContext()) {
            context.loadKlasses();
            var klass = context.getKlass(klassId);
            var nameField = klass.getFieldByName("name");
            var inst = (ClassInstance) context.get(instId);
            Assert.assertEquals("Leen2", Instances.toJavaString(inst.getField(nameField)));
            var inst2 = (ClassInstance) context.get(instId2);
            Assert.assertEquals(Instances.nullInstance(), inst2.getField(nameField));
        }
    }

    protected Id saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(Object qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }


}
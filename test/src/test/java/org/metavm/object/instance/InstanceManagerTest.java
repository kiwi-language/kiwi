package org.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.flow.FlowSavingContext;
import org.metavm.mocks.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.ClassInstanceWrap;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.LoadInstancesByPathsRequest;
import org.metavm.object.instance.rest.SelectRequest;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class InstanceManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceManagerTest.class);

    private InstanceManager instanceManager;
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
        instanceManager = managers.instanceManager();
        typeManager = managers.typeManager();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache()));
        ContextUtil.setAppId(TestConstants.APP_ID);
        FlowSavingContext.initConfig();
    }

    @Override
    protected void tearDown() throws Exception {
        instanceManager = null;
        entityContextFactory = null;
        schedulerAndWorker = null;
        typeManager = null;
        apiClient = null;
        FlowSavingContext.clearConfig();
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

    private Foo saveFoo() {
        return TestUtils.doInTransaction(() -> {
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
                return foo;
            }
        });
    }

    public void testLoadByPaths() {
        var foo = saveFoo();
        var id = foo.getId();
        var result = instanceManager.loadByPaths(
                new LoadInstancesByPathsRequest(
                        null,
                        List.of(
                                Constants.ID_PREFIX + id + ".bar",
                                Constants.ID_PREFIX + id + ".bar.code",
                                Constants.ID_PREFIX + id + ".bazList.*.bars.0.code",
                                Constants.ID_PREFIX + id + ".bazList.*.bars.1.code"
                        )
                )
        );

        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            Assert.assertEquals(
                    List.of(
                            context.getInstance(foo.getBar()).getReference().toDTO(),
                            Instances.createString("Bar001").toDTO(),
                            Instances.createString("Bar002").toDTO(),
                            Instances.createString("Bar004").toDTO(),
                            Instances.createString("Bar003").toDTO(),
                            Instances.createString("Bar005").toDTO()
                    ),
                    result
            );
        }
    }

    public void testSelect() {
        var foo = saveFoo();
        var fooType = ModelDefRegistry.getClassType(Foo.class);
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        var page = instanceManager.select(new SelectRequest(
                fooType.toExpression(),
                List.of(
                        "bar.code",
                        "qux"
                ),
                "name = \"Big Foo\"",
                1,
                20
        ));
        Assert.assertEquals(1, page.total());
        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            MatcherAssert.assertThat(page.data().get(0)[0],
                    InstanceDTOMatcher.of(Instances.createString("Bar001").toDTO()));
            MatcherAssert.assertThat(page.data().get(0)[1],
                    InstanceDTOMatcher.of(context.getInstance(foo.getQux()).toDTO()));
        }
    }

    public void testShopping() {
        var shoppingTypes = MockUtils.createShoppingTypes();
        var shoppingInstances = MockUtils.createShoppingInstances(shoppingTypes);
        Assert.assertNotNull(shoppingInstances.shoesProduct());
    }

    public void testUtils() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm", typeManager, schedulerAndWorker);
        var contains = (boolean) callMethod("Utils", "test", List.of(
                List.of("a", "b", "c"), List.of("d", "b")
        ));
        Assert.assertTrue(contains);

        var contains2 = (boolean) callMethod("Utils", "test2", List.of(
           List.of("a", "b", "c"), "d", "b")
        );
        Assert.assertTrue(contains2);
    }

    public void testGenericOverloading() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm", typeManager, schedulerAndWorker);
        var subId = saveInstance("Sub", Map.of());
        var result = (boolean) callMethod(subId, "test<string>", List.of("abc"));
        Assert.assertTrue(result);
    }

    public void testLambda() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm", typeManager, schedulerAndWorker);
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

    public void testRemoveNonPersistedChild() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        MockUtils.assemble(parentChildMasm, typeManager, schedulerAndWorker);
        var parentId = saveInstance("Parent", Map.of());
        callMethod(parentId, "test", List.of());
        var parent = getObject(parentId);
        var children = parent.getArray("children");
        Assert.assertEquals(0, children.size());
        try {
            callMethod(parentId, "test2", List.of());
            Assert.fail("Should not be able to delete non-persisted child when it's referenced");
        } catch (BusinessException e) {
            Assert.assertSame(e.getErrorCode(), ErrorCode.STRONG_REFS_PREVENT_REMOVAL);
        }
    }

    public void testRemoveRoot() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        MockUtils.assemble(parentChildMasm, typeManager, schedulerAndWorker);
        var parentId = saveInstance("Parent", Map.of());
        deleteObject(parentId);
    }

    public void testRelocation() {
        var klassIds = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var productKlass = context.bind(TestUtils.newKlassBuilder("Product").build());
                var inventoryKlass = context.bind(TestUtils.newKlassBuilder("Inventory").build());
                FieldBuilder.newBuilder("inventory", productKlass, Types.getNullableType(inventoryKlass.getType()))
                        .isChild(true)
                        .build();
                FieldBuilder.newBuilder("quantity", inventoryKlass, PrimitiveType.longType)
                        .build();
                context.finish();
                return new Id[]{productKlass.getId(), inventoryKlass.getId()};
            }
        });
        var productKlassId = klassIds[0];
        var inventoryKlassId = klassIds[1];
        var ids = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var productKlass = context.getKlass(productKlassId);
                var inventoryKlass = context.getKlass(inventoryKlassId);
                var product = ClassInstance.create(Map.of(), productKlass.getType());
                context.getInstanceContext().bind(product);
                var inventory = ClassInstance.create(
                        Map.of(
                                inventoryKlass.getFieldByName("quantity"),
                                Instances.longInstance(0)
                        ),
                        inventoryKlass.getType()
                );
                context.getInstanceContext().bind(inventory);
                context.finish();
                return new Id[]{product.getId(), inventory.getId()};
            }
        });
        var productId = ids[0];
        var inventoryId = ids[1];
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var product = (ClassInstance) context.getInstanceContext().get(productId);
                var inventory = context.getInstanceContext().get(inventoryId);
//                context.getInstanceContext().remove(product);
                product.setField(product.getKlass().getFieldByName("inventory"), inventory.getReference());
                context.finish();
                Assert.assertEquals(inventory, inventory.getRoot());
                Assert.assertEquals(inventory.getTreeId(), inventory.getTreeId());
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID, builder -> builder.relocationEnabled(true))) {
                var product = (ClassInstance) context.getInstanceContext().get(productId);
                var inventory = context.getInstanceContext().get(inventoryId);
//                context.getInstanceContext().remove(product);
                context.finish();
                Assert.assertEquals(product, inventory.getRoot());
                Assert.assertEquals(inventoryId, inventory.getId());
            }
        });
//        try (var context = newContext()) {
//            var inventory = context.getInstanceContext().get(inventoryId);
//            try {
//                inventory.ensureLoaded();
//                Assert.fail();
//            } catch (BusinessException e) {
//                Assert.assertEquals(String.format("Object '%s' not found", inventoryId.toString()), e.getMessage());
//            }
//        }
    }

    public void testIndexQuery() {
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var foo = new IndexFoo();
                context.bind(foo);
                context.finish();
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var fooByState = context.selectFirstByKey(IndexFoo.IDX_STATE, FooState.STATE1);
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
                var klass = context.getKlass(klassId);
                var instCtx = context.getInstanceContext();
                var inst = ClassInstanceBuilder.newBuilder(klass.getType())
                        .data(Map.of(
                                klass.getFieldByName("name"),
                                Instances.stringInstance("Leen")
                        )).build();
                instCtx.bind(inst);
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
                var klass = context.getKlass(klassId);
                var nameField = klass.getFieldByName("name");
                Assert.assertTrue(nameField.isMetadataRemoved());
                var instCtx = context.getInstanceContext();
                var inst = (ClassInstance) instCtx.get(instId);
                Assert.assertTrue(inst.isFieldInitialized(nameField));
                inst.setField(nameField, Instances.stringInstance("Leen2"));
                var inst2 = ClassInstanceBuilder.newBuilder(klass.getType())
                        .data(Map.of())
                        .build();
                instCtx.bind(inst2);
                context.finish();
                return inst2.getId();
            }
        });
        try(var context = newContext()) {
            var klass = context.getKlass(klassId);
            var nameField = klass.getFieldByName("name");
            var instCtx = context.getInstanceContext();
            var inst = (ClassInstance) instCtx.get(instId);
            Assert.assertEquals(Instances.stringInstance("Leen2"), inst.getField(nameField));
            var inst2 = (ClassInstance) instCtx.get(instId2);
            Assert.assertEquals(Instances.nullInstance(), inst2.getField(nameField));
        }
    }

    protected String saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(String qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected ClassInstanceWrap getObject(String id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }

    protected void deleteObject(String id) {
        TestUtils.doInTransactionWithoutResult(() -> apiClient.deleteInstance(id));
    }


}
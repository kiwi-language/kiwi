package org.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.flow.FlowExecutionService;
import org.metavm.flow.FlowSavingContext;
import org.metavm.flow.rest.FlowExecutionRequest;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.mocks.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.DefaultViewId;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.view.rest.dto.DirectMappingKey;
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
    private FlowExecutionService flowExecutionService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        schedulerAndWorker = bootResult.schedulerAndWorker();
        entityContextFactory = bootResult.entityContextFactory();
        instanceManager = managers.instanceManager();
        typeManager = managers.typeManager();
        flowExecutionService = managers.flowExecutionService();
        ContextUtil.setAppId(TestConstants.APP_ID);
        FlowSavingContext.initConfig();
    }

    @Override
    protected void tearDown() throws Exception {
        instanceManager = null;
        entityContextFactory = null;
        schedulerAndWorker = null;
        typeManager = null;
        flowExecutionService = null;
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
        var utilsType = typeManager.getTypeByCode("Utils").type();
        var contains = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(utilsType, "test"),
                        null,
                        List.of(
                                new ListFieldValue(
                                        null,
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createString("a"),
                                                PrimitiveFieldValue.createString("b"),
                                                PrimitiveFieldValue.createString("c")
                                        )
                                ),
                                new ListFieldValue(
                                        null,
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createString("d"),
                                                PrimitiveFieldValue.createString("b")
                                        )
                                )
                        )
                )
        ));
        Assert.assertEquals("true", contains.title());

        var contains2 = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(utilsType, "test2"),
                        null,
                        List.of(
                                new ListFieldValue(
                                        null,
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createString("a"),
                                                PrimitiveFieldValue.createString("b"),
                                                PrimitiveFieldValue.createString("c")
                                        )
                                ),
                                PrimitiveFieldValue.createString("b"),
                                PrimitiveFieldValue.createString("d")
                        )
                )
        ));
        Assert.assertEquals("true", contains2.title());
    }

    public void testGenericOverloading() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm", typeManager, schedulerAndWorker);
        var baseType = typeManager.getTypeByCode("Base").type();
        var subType = typeManager.getTypeByCode("Sub").type();
        var testMethodId = TestUtils.getMethodByCode(baseType, "test").id();
        var subId = TestUtils.doInTransaction(() -> instanceManager.create(
                InstanceDTO.createClassInstance(
                        Constants.ID_PREFIX + subType.id(),
                        List.of()
                )
        ));
        var result = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        new MethodRefDTO(
                                TypeExpressions.getClassType(baseType.id()),
                                testMethodId,
                                List.of("string")
                        ),
                        subId,
                        List.of(PrimitiveFieldValue.createString("abc"))
                )
        ));
        Assert.assertEquals("true", result.title());
    }

    public void testLambda() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm", typeManager, schedulerAndWorker);
        var utilsType = typeManager.getTypeByCode("Utils").type();
        var result = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(utilsType, "findGt"),
                        null,
                        List.of(
                                new ListFieldValue(
                                        null,
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createLong(1),
                                                PrimitiveFieldValue.createLong(2),
                                                PrimitiveFieldValue.createLong(3)
                                        )
                                ),
                                PrimitiveFieldValue.createLong(2)
                        )
                )
        ));
        Assert.assertEquals("3", result.title());
    }

    public void testLivingBeing() {
        var typeIds = MockUtils.createLivingBeingTypes(typeManager, schedulerAndWorker);
        var human = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        new MethodRefDTO(TypeExpressions.getClassType(typeIds.humanTypeId()), typeIds.humanConstructorId(), List.of()),
                        null,
                        List.of(
                                PrimitiveFieldValue.createLong(30),
                                PrimitiveFieldValue.createNull(),
                                PrimitiveFieldValue.createLong(180),
                                PrimitiveFieldValue.createString("Inventor")
                        )
                )
        ));
        Assert.assertEquals("30", human.getFieldValue(typeIds.livingBeingAgeFieldId()).getDisplayValue());
        Assert.assertEquals("null", human.getFieldValue(typeIds.livingBeingExtraFieldId()).getDisplayValue());
        Assert.assertEquals("180", human.getFieldValue(typeIds.animalIntelligenceFieldId()).getDisplayValue());
        Assert.assertEquals("Inventor", human.getFieldValue(typeIds.humanOccupationFieldId()).getDisplayValue());
        Assert.assertEquals("false", human.getFieldValue(typeIds.humanThinkingFieldId()).getDisplayValue());
        var makeSoundResult = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.createMethodRef(typeIds.livingBeingTypeId(), typeIds.makeSoundMethodId()),
                        human.id(),
                        List.of()
                )
        ));
        Assert.assertEquals("I am a human being", makeSoundResult.title());
        TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.createMethodRef(typeIds.sentientTypeId(), typeIds.thinkMethodId()),
                        human.id(),
                        List.of()
                )
        ));
        var reloadedHuman = instanceManager.get(human.id(), 2).instance();
        Assert.assertEquals("true", reloadedHuman.getFieldValue(typeIds.humanThinkingFieldId()).getDisplayValue());
    }

    public void testRemoveNonPersistedChild() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        MockUtils.assemble(parentChildMasm, typeManager, schedulerAndWorker);
        var parentType = typeManager.getTypeByCode("Parent").type();
        var parent = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(parentType, "Parent"),
                null,
                List.of()
        )));
        TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(parentType, "test"),
                parent.id(),
                List.of()
        )));
        var reloadedParent = instanceManager.get(parent.id(), 2).instance();
        var parentChildrenFieldId = TestUtils.getFieldIdByCode(parentType, "children");
        var children = ((InstanceFieldValue) reloadedParent.getFieldValue(parentChildrenFieldId)).getInstance();
        Assert.assertEquals(0, children.getListSize());

        try {
            TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    TestUtils.getMethodRefByCode(parentType, "test2"),
                    parent.id(),
                    List.of()
            )));
            Assert.fail("Should not be able to delete non-persisted child when it's referenced");
        } catch (BusinessException e) {
            Assert.assertSame(e.getErrorCode(), ErrorCode.STRONG_REFS_PREVENT_REMOVAL);
        }
    }

    public void testRemoveRoot() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        MockUtils.assemble(parentChildMasm, typeManager, schedulerAndWorker);
        var parentType = typeManager.getTypeByCode("Parent").type();
        var parent = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(parentType, "Parent"),
                null,
                List.of()
        )));
        var mappingId = new DirectMappingKey(Id.parse(TestUtils.getDefaultMapping(parentType).id()));
        var viewId = new DefaultViewId(false, mappingId, Id.parse(parent.id()));
//        var parentMapping = instanceManager.get(viewId.toString(), 2);
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(viewId.toString()));
    }

    public void testRelocation() {
        var klassIds = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var productKlass = context.bind(TestUtils.newKlassBuilder("Product").build());
                var inventoryKlass = context.bind(TestUtils.newKlassBuilder("Inventory").build());
                FieldBuilder.newBuilder("inventory", "inventory", productKlass, Types.getNullableType(inventoryKlass.getType()))
                        .isChild(true)
                        .build();
                FieldBuilder.newBuilder("quantity", "quantity", inventoryKlass, PrimitiveType.longType)
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
                                inventoryKlass.getFieldByCode("quantity"),
                                Instances.longInstance(0L)
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
                product.setField(product.getKlass().getFieldByCode("inventory"), inventory.getReference());
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
                FieldBuilder.newBuilder("name", "name", klass, Types.getStringType()).build();
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
                                klass.getFieldByCode("name"),
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
                var nameField = klass.getFieldByCode("name");
                nameField.setMetadataRemoved();
                context.finish();
            }
        });
        var instId2 = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var klass = context.getKlass(klassId);
                var nameField = klass.getFieldByCode("name");
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
            var nameField = klass.getFieldByCode("name");
            var instCtx = context.getInstanceContext();
            var inst = (ClassInstance) instCtx.get(instId);
            Assert.assertEquals(Instances.stringInstance("Leen2"), inst.getField(nameField));
            var inst2 = (ClassInstance) instCtx.get(instId2);
            Assert.assertEquals(Instances.nullInstance(), inst2.getField(nameField));
        }
    }

}
package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.asm.AssemblerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;

public class InstanceManagerTest extends TestCase {

    private InstanceManager instanceManager;
    private EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    private FlowManager flowManager;
    private FlowExecutionService flowExecutionService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var instanceSearchService = bootResult.instanceSearchService();
        var instanceQueryService = new InstanceQueryService(instanceSearchService);
        entityContextFactory = bootResult.entityContextFactory();
        instanceManager = new InstanceManager(entityContextFactory, bootResult.instanceStore(), instanceQueryService);
        ContextUtil.setAppId(TestConstants.APP_ID);
        var entityQueryService = new EntityQueryService(instanceQueryService);
        var transactionOperations = new MockTransactionOperations();
        typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                entityQueryService,
                new TaskManager(entityContextFactory, transactionOperations),
                transactionOperations
        );
        flowManager = new FlowManager(entityContextFactory);
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        flowExecutionService = new FlowExecutionService(entityContextFactory);
        typeManager.setFlowExecutionService(flowExecutionService);
        FlowSavingContext.initConfig();
    }

    @Override
    protected void tearDown() throws Exception {
        instanceManager = null;
        entityContextFactory = null;
        typeManager = null;
        flowManager = null;
        flowExecutionService = null;
        FlowSavingContext.clearConfig();
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(false);
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
                                Constants.CONSTANT_ID_PREFIX + id + ".巴",
                                Constants.CONSTANT_ID_PREFIX + id + ".巴.编号",
                                Constants.CONSTANT_ID_PREFIX + id + ".巴子.*.巴列表.0.编号",
                                Constants.CONSTANT_ID_PREFIX + id + ".巴子.*.巴列表.1.编号"
                        )
                )
        );

        try (var context = newContext()) {
            foo = context.getEntity(Foo.class, foo.getId());
            Assert.assertEquals(
                    List.of(
                            context.getInstance(foo.getBar()).toDTO(),
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
        var page = instanceManager.select(new SelectRequest(
                fooType.getStringId(),
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
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm", typeManager);
        var utilsType = typeManager.getTypeByCode("Utils").type();
        var contains = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodByCode(utilsType, "test").id(),
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
        Assert.assertEquals("是", contains.title());

        var contains2 = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodIdByCode(utilsType, "test2"),
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
        Assert.assertEquals("是", contains2.title());
    }

    public void testLivingBeing() {
        var typeIds = MockUtils.createLivingBeingTypes(typeManager);
        var human = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        typeIds.humanConstructorId(),
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
        Assert.assertEquals("空", human.getFieldValue(typeIds.livingBeingExtraFieldId()).getDisplayValue());
        Assert.assertEquals("180", human.getFieldValue(typeIds.animalIntelligenceFieldId()).getDisplayValue());
        Assert.assertEquals("Inventor", human.getFieldValue(typeIds.humanOccupationFieldId()).getDisplayValue());
        Assert.assertEquals("否", human.getFieldValue(typeIds.humanThinkingFieldId()).getDisplayValue());
        var isMultiCellular = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        typeIds.makeSoundMethodId(),
                        human.id(),
                        List.of()
                )
        ));
        Assert.assertEquals("I am a human being", isMultiCellular.title());
        TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        typeIds.thinkMethodId(),
                        human.id(),
                        List.of()
                )
        ));
        var reloadedHuman = instanceManager.get(human.id(), 2).instance();
        Assert.assertEquals("是", reloadedHuman.getFieldValue(typeIds.humanThinkingFieldId()).getDisplayValue());
    }

    public void testRemoveChildInUse() {
        var childType = TestUtils.doInTransaction(() -> typeManager.saveType(
                ClassTypeDTOBuilder.newBuilder("Child")
                        .build()
        ));
        var nullableChildType = typeManager.getUnionType(List.of(childType.id(), StandardTypes.getNullType().getStringId())).type();
        var typeTmpId = TmpId.random().toString();
        var childFieldTmpId = TmpId.random().toString();
        var childRefFieldTmpId = TmpId.random().toString();
        var parentType = TestUtils.doInTransaction(() -> typeManager.saveType(
                ClassTypeDTOBuilder.newBuilder("Parent")
                        .id(typeTmpId)
                        .addField(
                                FieldDTOBuilder.newBuilder("child", nullableChildType.id())
                                        .code("child")
                                        .id(childFieldTmpId)
                                        .isChild(true)
                                        .build()
                        )
                        .addField(
                                FieldDTOBuilder.newBuilder("childRef", childType.id())
                                        .code("childRef")
                                        .id(childRefFieldTmpId)
                                        .build()
                        )
                        .addMethod(
                                MethodDTOBuilder.newBuilder(typeTmpId, "Parent")
                                        .code("Parent")
                                        .tmpId(NncUtils.randomNonNegative())
                                        .returnTypeId(typeTmpId)
                                        .isConstructor(true)
                                        .addNode(
                                                NodeDTOFactory.createSelfNode(NncUtils.randomNonNegative(), "self", typeTmpId)
                                        )
                                        .addNode(
                                                NodeDTOFactory.createAddObjectNode(
                                                        NncUtils.randomNonNegative(),
                                                        "child",
                                                        childType.id(),
                                                        List.of()
                                                )
                                        )
                                        .addNode(
                                                NodeDTOFactory.createUpdateObjectNode(
                                                        NncUtils.randomNonNegative(),
                                                        "init",
                                                        ValueDTOFactory.createReference("self"),
                                                        List.of(
                                                                new UpdateFieldDTO(
                                                                        childFieldTmpId,
                                                                        null,
                                                                        UpdateOp.SET.code(),
                                                                        ValueDTOFactory.createReference("child")
                                                                ),
                                                                new UpdateFieldDTO(
                                                                        childRefFieldTmpId,
                                                                        null,
                                                                        UpdateOp.SET.code(),
                                                                        ValueDTOFactory.createReference("child")
                                                                )
                                                        )
                                                )
                                        )
                                        .addNode(
                                                NodeDTOFactory.createReturnNode(
                                                        NncUtils.randomNonNegative(),
                                                        "Return",
                                                        ValueDTOFactory.createReference("self")
                                                )
                                        )
                                        .build()
                        )
                        .build()
        ));

        var parentConstructorId = TestUtils.getMethodIdByCode(parentType, "Parent");
        var childFieldId = TestUtils.getFieldIdByCode(parentType, "child");
        var childRefFieldId = TestUtils.getFieldIdByCode(parentType, "childRef");
        var parentId = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        parentConstructorId,
                        null,
                        List.of()
                )
        )).id();
        var parent = instanceManager.get(parentId, 1).instance();
        var child = ((InstanceFieldValue) parent.getFieldValue(childFieldId)).getInstance();
        try {
            TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(
                    InstanceDTO.createClassInstance(
                            parent.id(),
                            parentType.id(),
                            List.of(
                                    InstanceFieldDTO.create(
                                            childFieldId,
                                            PrimitiveFieldValue.createNull()
                                    ),
                                    InstanceFieldDTO.create(
                                            childRefFieldId,
                                            ReferenceFieldValue.create(child.id())
                                    )
                            )
                    )
            ));
            Assert.fail("Should not be able to delete child in use");
        }
        catch (BusinessException e) {
            Assert.assertEquals(String.format("对象被其他对象关联，无法删除: %s-%s", childType.name(), child.title()), e.getMessage());
        }
    }

    public void testRemoveNonPersistedChild() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        var assembler = AssemblerFactory.createWithStandardTypes();
        var types = assembler.assemble(List.of(parentChildMasm));
        FlowSavingContext.initConfig();
        TestUtils.doInTransaction(() -> typeManager.batchSave(new BatchSaveRequest(types, List.of(), List.of(), false)));
        var parentType = typeManager.getTypeByCode("Parent").type();
        var parentConstructorId = TestUtils.getMethodByCode(parentType, "Parent").id();
        var parent = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                parentConstructorId,
                null,
                List.of()
        )));
        var parentTestMethodId = TestUtils.getMethodIdByCode(parentType, "test");
        TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                parentTestMethodId,
                parent.id(),
                List.of()
        )));
        var reloadedParent = instanceManager.get(parent.id(), 2).instance();
        var parentChildrenFieldId = TestUtils.getFieldIdByCode(parentType, "children");
        var children = ((InstanceFieldValue) reloadedParent.getFieldValue(parentChildrenFieldId)).getInstance();
        Assert.assertEquals(0, children.getListSize());

//        DebugEnv.DEBUG_ON = true;
        var parentTest2MethodId = TestUtils.getMethodIdByCode(parentType, "test2");
        try {
            TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    parentTest2MethodId,
                    parent.id(),
                    List.of()
            )));
            Assert.fail("Should not be able to delete non-persisted child when it's referenced");
        }
        catch (BusinessException e) {
            Assert.assertSame(e.getErrorCode(), ErrorCode.STRONG_REFS_PREVENT_REMOVAL);
        }
    }

    public void testRemoveRoot() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        var assembler = AssemblerFactory.createWithStandardTypes();
        var types = assembler.assemble(List.of(parentChildMasm));
        FlowSavingContext.initConfig();
        TestUtils.doInTransaction(() -> typeManager.batchSave(new BatchSaveRequest(types, List.of(), List.of(), false)));
        var parentType = typeManager.getTypeByCode("Parent").type();
        var parentConstructorId = TestUtils.getMethodByCode(parentType, "Parent").id();
        var parent = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                parentConstructorId,
                null,
                List.of()
        )));
        var mappingId = Id.parse(TestUtils.getDefaultMapping(parentType).id());
        var viewId = new DefaultViewId(false, mappingId, Id.parse(parent.id()));
//        var parentMapping = instanceManager.get(viewId.toString(), 2);
//        DebugEnv.DEBUG_ON = true;
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(viewId.toString()));
    }

}
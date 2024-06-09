package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.flow.rest.MethodRefDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.TypeExpressions;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;
import tech.metavm.object.type.rest.dto.FieldRefDTO;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.view.rest.dto.DirectMappingKey;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;

public class InstanceManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceManagerTest.class);

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
                new TaskManager(entityContextFactory, transactionOperations)
        );
        flowManager = new FlowManager(entityContextFactory, new MockTransactionOperations());
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
                                Constants.CONSTANT_ID_PREFIX + id + ".bar",
                                Constants.CONSTANT_ID_PREFIX + id + ".bar.code",
                                Constants.CONSTANT_ID_PREFIX + id + ".bazList.*.bars.0.code",
                                Constants.CONSTANT_ID_PREFIX + id + ".bazList.*.bars.1.code"
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
                fooType.toExpression(),
                List.of(
                        "bar.code",
                        "qux"
                ),
                "name = 'Big Foo'",
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
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm", typeManager);
        var baseType = typeManager.getTypeByCode("Base").type();
        var subType = typeManager.getTypeByCode("Sub").type();
        var testMethodId = TestUtils.getMethodByCode(baseType, "test").id();
        var subId = TestUtils.doInTransaction(() -> instanceManager.create(
                InstanceDTO.createClassInstance(
                        Constants.CONSTANT_ID_PREFIX + subType.id(),
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
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm", typeManager);
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
        var typeIds = MockUtils.createLivingBeingTypes(typeManager);
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
        var humanType = typeManager.getType(new GetTypeRequest(typeIds.humanTypeId(), false)).type();
        var method = TestUtils.getMethodByCode(humanType, "makeSound");
        logger.info("overridden of Human.makeSound: {}", ((MethodParam) method.param()).overriddenRefs());
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

    public void testRemoveChildInUse() {
        var childType = TestUtils.doInTransaction(() -> typeManager.saveType(
                ClassTypeDTOBuilder.newBuilder("Child").tmpId(NncUtils.randomNonNegative()).build()
        ));
        var nullableChildType = TypeExpressions.getNullableType(TypeExpressions.getClassType(childType.id()));
        var typeTmpId = TmpId.random().toString();
        var typeExpr = TypeExpressions.getClassType(typeTmpId);
        var childFieldTmpId = TmpId.random().toString();
        var childRefFieldTmpId = TmpId.random().toString();
        var parentType = TestUtils.doInTransaction(() -> typeManager.saveType(
                ClassTypeDTOBuilder.newBuilder("Parent")
                        .id(typeTmpId)
                        .addField(
                                FieldDTOBuilder.newBuilder("child", nullableChildType)
                                        .code("child")
                                        .id(childFieldTmpId)
                                        .isChild(true)
                                        .build()
                        )
                        .addField(
                                FieldDTOBuilder.newBuilder("childRef", TypeExpressions.getClassType(childType.id()))
                                        .code("childRef")
                                        .id(childRefFieldTmpId)
                                        .build()
                        )
                        .addMethod(
                                MethodDTOBuilder.newBuilder(typeTmpId, "Parent")
                                        .code("Parent")
                                        .tmpId(NncUtils.randomNonNegative())
                                        .returnType(typeExpr)
                                        .isConstructor(true)
                                        .addNode(
                                                NodeDTOFactory.createSelfNode(NncUtils.randomNonNegative(), "self", typeExpr)
                                        )
                                        .addNode(
                                                NodeDTOFactory.createAddObjectNode(
                                                        NncUtils.randomNonNegative(),
                                                        "child",
                                                        TypeExpressions.getClassType(childType.id()),
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
                                                                        new FieldRefDTO(
                                                                                typeExpr,
                                                                                childFieldTmpId
                                                                        ),
                                                                        null,
                                                                        UpdateOp.SET.code(),
                                                                        ValueDTOFactory.createReference("child")
                                                                ),
                                                                new UpdateFieldDTO(
                                                                        new FieldRefDTO(
                                                                                typeExpr,
                                                                                childRefFieldTmpId
                                                                        ),
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
                                                        "return",
                                                        ValueDTOFactory.createReference("self")
                                                )
                                        )
                                        .build()
                        )
                        .build()
        ));

        var childFieldId = TestUtils.getFieldIdByCode(parentType, "child");
        var childRefFieldId = TestUtils.getFieldIdByCode(parentType, "childRef");
        var parentId = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(parentType, "Parent"),
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
                            TypeExpressions.getClassType(parentType.id()),
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
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("Object is referenced by other objects, cannot be deleted: %s-%s", childType.name(), child.title()), e.getMessage());
        }
    }

    public void testRemoveNonPersistedChild() {
        final var parentChildMasm = "/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm";
        MockUtils.assemble(parentChildMasm, typeManager);
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
        MockUtils.assemble(parentChildMasm, typeManager);
        var parentType = typeManager.getTypeByCode("Parent").type();
        var parent = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(parentType, "Parent"),
                null,
                List.of()
        )));
        var mappingId = new DirectMappingKey(Id.parse(TestUtils.getDefaultMapping(parentType).id()));
        var viewId = new DefaultViewId(false, mappingId, Id.parse(parent.id()));
//        var parentMapping = instanceManager.get(viewId.toString(), 2);
//        DebugEnv.DEBUG_ON = true;
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(viewId.toString()));
    }

    public void testValueInstance() {
        var ref = new Object() {
            Id id;
        };
        var classType = new ClassType(StandardTypes.getChildListKlass(), List.of(StandardTypes.getStringType()));
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var klass = ClassTypeBuilder.newBuilder("Foo", null).build();
                var method = MethodBuilder.newBuilder(klass, "test", null).build();
                var methodCallNode = new MethodCallNode(
                        null,
                        "call",
                        null,
                        null,
                        method.getRootScope(),
                        null,
                        new MethodRef(
                                classType,
                                StandardTypes.getChildListKlass().getMethodByCodeAndParamTypes("size", List.of()),
                                List.of()
                        ),
                        List.of()
                );

                context.bind(klass);
                context.finish();
                ref.id = methodCallNode.getId();
            }
        });
        try (var context = newContext()) {
            var node = context.getEntity(MethodCallNode.class, ref.id);
            Assert.assertEquals(classType, node.getFlowRef().getDeclaringType());
        }
    }

}
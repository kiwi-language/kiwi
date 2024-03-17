package tech.metavm.object.view;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.mocks.MockNativeFunctionsInitializer;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Nodes;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Values;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.view.mocks.MockMappingRepository;
import tech.metavm.util.Instances;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static tech.metavm.entity.StandardTypes.getStringType;

public class MappingSaverTest extends TestCase {

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(MappingSaverTest.class);

    private InstanceRepository instanceRepository;
    private TypeProviders typeProviders;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instanceRepository = new MockInstanceRepository();
        typeProviders = new TypeProviders();
        MockStandardTypesInitializer.init();
        MockNativeFunctionsInitializer.init(typeProviders.functionTypeProvider);
    }

    public void testFromView() {
        var typeRepository = new MockTypeRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .build();
        FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .asTitle()
                .build();
        TestUtils.initEntityIds(fooType);
        var mapping = saver.saveBuiltinMapping(fooType, true);
        var fromViewMethod = MethodBuilder.newBuilder(fooType, "从视图创建", "fromView", typeProviders.functionTypeProvider)
                .parameters(new Parameter(null, "view", "view", mapping.getTargetType()))
                .returnType(fooType)
                .build();
        TestUtils.initEntityIds(fromViewMethod);
        saver.saveBuiltinMapping(fooType, true);
        logger.info(mapping.getUnmapper().getText());
    }

    public void testSaveBuiltin() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .build();
        var barType = ClassTypeBuilder.newBuilder("Bar", "Bar")
                .build();

        var barChildArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.CHILD);
        var barReadWriteArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.READ_WRITE);

        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barChildArrayType)
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .asTitle()
                .build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, getStringType())
                .asTitle()
                .build();

        // generate getBars method
        var getBarsMethod = MethodBuilder.newBuilder(fooType, "获取bars", "getBars", typeProviders.functionTypeProvider)
                .returnType(barReadWriteArrayType)
                .build();
        {
            var scope = getBarsMethod.getRootScope();
            var selfNode = Nodes.self("Self", null, fooType, scope);
            var barsNode = Nodes.newArray("NewArray", null, barReadWriteArrayType,
                    Values.nodeProperty(selfNode, fooBarsField), null, scope);
            Nodes.ret("Return", scope, Values.node(barsNode));
        }

        // generate setBars method
        var setBarsMethod = MethodBuilder.newBuilder(fooType, "设置bars", "setBars", typeProviders.functionTypeProvider)
                .parameters(new Parameter(null, "bars", "bars", barReadWriteArrayType))
                .build();
        {
            var scope = setBarsMethod.getRootScope();
            var selfNode = Nodes.self("Self", null, fooType, scope);
            var inputNode = Nodes.input(setBarsMethod);
            Nodes.clearArray("ClearBars", null, Values.nodeProperty(selfNode, fooBarsField), scope);
            Nodes.forEach("循环", () -> Values.inputValue(inputNode, 0),
                    (bodyScope, element, index) -> {
                        Nodes.addElement("AddBar", null, Values.nodeProperty(selfNode, fooBarsField),
                                element.get(), bodyScope);
                    },
                    scope);
            Nodes.ret("Return", scope, null);
        }
        TestUtils.initEntityIds(fooType);

        var typeRepository = new MockTypeRepository();
        typeRepository.save(fooType);
        typeRepository.save(barType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );

        var barMapping = saver.saveBuiltinMapping(barType, true);
        var fooMapping = saver.saveBuiltinMapping(fooType, true);
        var fooViewType = fooMapping.getTargetType();
        var barsFieldMapping = fooMapping.getFieldMappingByTargetField(fooViewType.getFieldByCode("bars"));
//        var barArrayMapping = Objects.requireNonNull(barsFieldMapping.getNestedMapping());
//        fooMapping.initId(3001L);
//        barArrayMapping.initId(3002L);
//        barMapping.initId(3003L);

        var barInst = ClassInstanceBuilder.newBuilder(barType)
                .data(Map.of(
                        barCodeField,
                        new StringInstance("bar001", StandardTypes.getStringType())
                ))
                .build();

        var barChildArray = new ArrayInstance(barChildArrayType, List.of(barInst));

        var foo = ClassInstanceBuilder.newBuilder(fooType)
                .data(
                        Map.of(
                                fooNameField,
                                new StringInstance("foo", StandardTypes.getStringType()),
                                fooBarsField,
                                barChildArray
                        )
                )
                .build();

        TestUtils.initInstanceIds(foo);

//        logger.info(barArrayMapping.getMapper().getText());
//        logger.info(barArrayMapping.getUnmapper().getText());

        TestUtils.initEntityIds(fooType);

        // Mapping
        var fooView = (ClassInstance) fooMapping.mapRoot(foo, instanceRepository, typeProviders.parameterizedFlowProvider);

        Assert.assertSame(fooView.getType(), fooViewType);
        Assert.assertSame(foo, fooView.tryGetSource());
        var fooViewName = fooView.getField("name");
        Assert.assertEquals(foo.getField("name"), fooViewName);
        Assert.assertTrue(fooView.getId() instanceof ViewId);

        var fooViewBars = (ArrayInstance) fooView.getField("bars");
        Assert.assertTrue(fooViewBars.isChildArray());
        var barView = (ClassInstance) fooViewBars.get(0);
        var barViewType = barMapping.getTargetType();
        Assert.assertSame(barView.getType(), barViewType);
        Assert.assertEquals(barInst.getField("code"), barView.getField("code"));
        Assert.assertTrue(barView.getId() instanceof ChildViewId);

        // mapping
        logger.info(fooMapping.getMapper().getText());
        logger.info(fooMapping.getReadMethod().getText());

        // Unmapping
        logger.info(fooMapping.getUnmapper().getText());
        logger.info(fooMapping.getWriteMethod().getText());

        fooView.setField("name", Instances.stringInstance("foo2"));
        barView.setField("code", Instances.stringInstance("bar002"));

//        fooViewBars.addElement(
//                ClassInstanceBuilder.newBuilder(barMapping.getTargetType())
//                        .data(Map.of(
//                                barViewType.getFieldByCode("code"),
//                                Instances.stringInstance("bar002")
//                        ))
//                        .build()
//        );

        var unmappedFoo =
                fooMapping.unmap(fooView, instanceRepository, typeProviders.parameterizedFlowProvider);
        Assert.assertSame(foo, unmappedFoo);
        Assert.assertEquals(Instances.stringInstance("foo2"), foo.getField("name"));
        var bars = (ArrayInstance) foo.getField("bars");
        Assert.assertEquals(1, bars.size());
        var bar = (ClassInstance) bars.get(0);
        Assert.assertEquals(Instances.stringInstance("bar002"), bar.getField("code"));
    }

    public void testOrder() {
        var shoppingTypes = MockUtils.createShoppingTypes();
        TestUtils.initEntityIds(shoppingTypes.orderType());
        typeProviders.addType(shoppingTypes.orderType());
        typeProviders.addType(shoppingTypes.productType());
        typeProviders.addType(shoppingTypes.skuType());
        typeProviders.addType(shoppingTypes.couponType());
        typeProviders.addType(shoppingTypes.couponStateType());
        typeProviders.addType(shoppingTypes.couponArrayType());
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeProviders.typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );
        var orderMapping = (FieldsObjectMapping) saver.saveBuiltinMapping(shoppingTypes.orderType(), true);
        TestUtils.initEntityIds(shoppingTypes.orderType());

        var order = ClassInstanceBuilder.newBuilder(shoppingTypes.orderType())
                .data(Map.of(
                        shoppingTypes.orderAmountField(),
                        Instances.longInstance(1L),
                        shoppingTypes.orderCodeField(),
                        Instances.stringInstance("001"),
                        shoppingTypes.orderCouponsField(),
                        new ArrayInstance(
                                shoppingTypes.couponArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(shoppingTypes.couponType())
                                                .data(Map.of(
                                                        shoppingTypes.couponTitleField(),
                                                        Instances.stringInstance("鞋子减5元"),
                                                        shoppingTypes.couponDiscountField(),
                                                        Instances.longInstance(5L),
                                                        shoppingTypes.couponStateField(),
                                                        shoppingTypes.couponNormalState()
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(shoppingTypes.couponType())
                                                .data(Map.of(
                                                        shoppingTypes.couponTitleField(),
                                                        Instances.stringInstance("鞋子减10元"),
                                                        shoppingTypes.couponDiscountField(),
                                                        Instances.longInstance(10L),
                                                        shoppingTypes.couponStateField(),
                                                        shoppingTypes.couponNormalState()
                                                ))
                                                .build()
                                )
                        ),
                        shoppingTypes.orderPriceField(),
                        Instances.doubleInstance(85.0),
                        shoppingTypes.orderProductField(),
                        ClassInstanceBuilder.newBuilder(shoppingTypes.productType())
                                .data(Map.of(
                                        shoppingTypes.productTitleField(),
                                        Instances.stringInstance("鞋子"),
                                        shoppingTypes.productSkuListField(),
                                        new ArrayInstance(
                                                shoppingTypes.skuChildArrayType(),
                                                List.of()
                                        )
                                ))
                                .build(),
                        shoppingTypes.orderTimeField(),
                        Instances.timeInstance(System.currentTimeMillis())
                ))
                .build();
        logger.info(orderMapping.getReadMethod().getText());
        logger.info(orderMapping.getMapper().getText());
        var oderView = orderMapping.mapRoot(order, instanceRepository, typeProviders.parameterizedFlowProvider);
    }

    public void testPathId() {
        var productType = ClassTypeBuilder.newBuilder("Product", "Product").build();
        var skuType = ClassTypeBuilder.newBuilder("Sku", "Sku").build();
        var skuChildArrayType = new ArrayType(null, skuType, ArrayKind.CHILD);
        var skuListField = FieldBuilder.newBuilder("skuList", "skuList", productType, skuChildArrayType)
                .isChild(true)
                .access(Access.PRIVATE)
                .build();
        var skuRwArrayType = new ArrayType(null, skuType, ArrayKind.READ_WRITE);
        var getSkuListMethod = MethodBuilder.newBuilder(productType, "getSkuList", "getSkuList", typeProviders.functionTypeProvider)
                .returnType(skuRwArrayType)
                .build();
        {
            var scope = getSkuListMethod.getRootScope();
            var self = Nodes.self("self", "self", productType, scope);
            Nodes.input(getSkuListMethod);
            var skuList = Nodes.newArray(
                    "skuList", "skuList", skuRwArrayType,
                    Values.nodeProperty(self, skuListField),
                    null, scope
            );
            Nodes.ret("Return", scope, Values.node(skuList));
        }

        var setSkuListMethod = MethodBuilder.newBuilder(productType, "setSkuList", "setSkuList", typeProviders.functionTypeProvider)
                .parameters(new Parameter(null, "skuList", "skuList", skuRwArrayType))
                .build();
        {
            var scope = setSkuListMethod.getRootScope();
            var self = Nodes.self("self", "self", productType, scope);
            var input = Nodes.input(setSkuListMethod);
            Nodes.clearArray("clearArray", null, Values.nodeProperty(self, skuListField), scope);
            Nodes.forEach(
                    "循环",
                    () -> Values.inputValue(input, 0),
                    (bodyScope, getElement, getIndex) -> {
                        Nodes.addElement("addElement", null,
                                Values.nodeProperty(self, skuListField), getElement.get(), bodyScope);
                    },
                    scope
            );
        }
        TestUtils.initEntityIds(productType);
        var typeRepository = new MockTypeRepository();
        typeRepository.save(productType);
        typeRepository.save(skuType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );
        saver.saveBuiltinMapping(skuType, true);
        var productMapping = saver.saveBuiltinMapping(productType, true);
        TestUtils.initEntityIds(productType);
        var sku = ClassInstanceBuilder.newBuilder(skuType)
                .data(Map.of())
                .build();
        var product = ClassInstanceBuilder.newBuilder(productType)
                .data(Map.of(
                        skuListField,
                        new ArrayInstance(skuChildArrayType, List.of(sku))
                ))
                .build();
        TestUtils.initInstanceIds(product);
        var viewSkuListField = productMapping.getTargetType().getFieldByCode("skuList");
        var productView = (ClassInstance) productMapping.mapRoot(product, instanceRepository, typeProviders.parameterizedFlowProvider);
        var skuListView = (ArrayInstance) productView.getField(viewSkuListField);
        Assert.assertTrue(productView.getId() instanceof ViewId);
        Assert.assertTrue(skuListView.getId() instanceof FieldViewId);
        Assert.assertTrue(skuListView.get(0).getId() instanceof ChildViewId);

        skuListView.removeElement(0);

        productMapping.unmap(productView, instanceRepository, typeProviders.parameterizedFlowProvider);
        var skuList = (ArrayInstance) product.getField(skuListField);
        Assert.assertTrue(skuList.isEmpty());
    }

    public void testUnionType() {
        var flowType = ClassTypeBuilder.newBuilder("流程", "Flow").build();
        var scopeType = ClassTypeBuilder.newBuilder("范围", "Scope").build();
        var scopeArrayType = new ArrayType(null, scopeType, ArrayKind.CHILD);
        var nullableScopeArrayType = new UnionType(null, Set.of(scopeArrayType, StandardTypes.getNullType()));
        var flowScopesField = FieldBuilder.newBuilder("范围列表", "scopes", flowType, nullableScopeArrayType).isChild(true).build();
        TestUtils.initEntityIds(flowType);
        var typeRepository = new MockTypeRepository();
        typeRepository.save(flowType);
        typeRepository.save(scopeType);
        typeRepository.save(scopeArrayType);
        typeRepository.save(nullableScopeArrayType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );
        var scopeMapping = saver.saveBuiltinMapping(scopeType, true);
        var flowMapping = saver.saveBuiltinMapping(flowType, true);
        var flowViewType = flowMapping.getTargetType();
        var flowViewScopesField = flowViewType.getFieldByCode("scopes");

        TestUtils.initEntityIds(flowType);

//        logger.info(flowMapping.getReadMethod().getText());
//        logger.info(flowMapping.getMapper().getText());
//        logger.info(flowMapping.getWriteMethod().getText());
//        logger.info(flowMapping.getUnmapper().getText());

        var flow = ClassInstanceBuilder.newBuilder(flowType)
                .data(Map.of(
                        flowScopesField,
                        new ArrayInstance(
                                scopeArrayType,
                                List.of(
                                        ClassInstanceBuilder.newBuilder(scopeType).build(),
                                        ClassInstanceBuilder.newBuilder(scopeType).build(),
                                        ClassInstanceBuilder.newBuilder(scopeType).build()
                                )
                        )
                ))
                .build();
        TestUtils.initInstanceIds(flow);

        var flowView = (ClassInstance) flowMapping.mapRoot(flow, instanceRepository, typeProviders.parameterizedFlowProvider);
        var flowViewScopes = (ArrayInstance) flowView.getField(flowViewScopesField);
        Assert.assertEquals(3, flowViewScopes.size());

        flowViewScopes.removeElement(2);
        flowMapping.unmap(flowView, instanceRepository, typeProviders.parameterizedFlowProvider);
        var flowScopes = (ArrayInstance) flow.getField(flowScopesField);
        Assert.assertEquals(2, flowScopes.size());

        flowView.setField(flowViewScopesField, Instances.nullInstance());
        flowMapping.unmap(flowView, instanceRepository, typeProviders.parameterizedFlowProvider);
        Assert.assertTrue(flow.getField(flowScopesField).isNull());
    }

    public void testGeneric() {
        var typeRepository = new MockTypeRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );

        var nodeType = ClassTypeBuilder.newBuilder("节点", "Node")
                .typeParameters(new TypeVariable(null, "值", "V", DummyGenericDeclaration.INSTANCE))
                .build();
        FieldBuilder.newBuilder("标签", "label", nodeType, getStringType())
                .asTitle()
                .build();
        FieldBuilder.newBuilder("值", "value", nodeType, nodeType.getTypeParameters().get(0))
                .build();

        var nodeMapping = saver.saveBuiltinMapping(nodeType, true);
        var listTypeVar = new TypeVariable(null, "值", "T", DummyGenericDeclaration.INSTANCE);
        var listType = ClassTypeBuilder.newBuilder("列表", "List")
                .typeParameters(listTypeVar)
                .build();
        var pNodeType = typeProviders.parameterizedTypeProvider.getParameterizedType(nodeType, List.of(listTypeVar),
                ResolutionStage.DEFINITION, new MockDTOProvider());
        var pNodeChildArrayType = new ArrayType(null, pNodeType, ArrayKind.CHILD);
        var listNodeField = FieldBuilder.newBuilder("nodes", "nodes", listType, pNodeChildArrayType).isChild(true).build();

        TestUtils.initEntityIds(listType);

        typeRepository.save(listType);

        var listMapping = saver.saveBuiltinMapping(listType, true);

        var listOfStrType = (ClassType) typeProviders.createFacade().getParameterizedType(listType,
                List.of(StandardTypes.getStringType()), ResolutionStage.DEFINITION, new MockDTOProvider());
        TestUtils.initEntityIds(listOfStrType);

        var listOfStrMapping = Objects.requireNonNull(listOfStrType.getDefaultMapping());
        var listOfStrNodesField = listOfStrType.getFieldByCode("nodes");
        var nodeOfStrChildArrayType = (ArrayType) listOfStrNodesField.getType();
        var nodeOfStrType = (ClassType) nodeOfStrChildArrayType.getElementType();
        var listOfStr = ClassInstanceBuilder.newBuilder(listOfStrType)
                .data(Map.of(
                        listOfStrNodesField,
                        new ArrayInstance(
                                nodeOfStrChildArrayType,
                                List.of(
                                        ClassInstanceBuilder.newBuilder(nodeOfStrType)
                                                .data(
                                                        Map.of(
                                                                nodeOfStrType.getFieldByCode("label"),
                                                                Instances.stringInstance("node001"),
                                                                nodeOfStrType.getFieldByCode("value"),
                                                                Instances.stringInstance("Hello")
                                                        )
                                                )
                                                .build()
                                )
                        )
                ))
                .build();
        TestUtils.initInstanceIds(listOfStr);
        var listOfStrView = listOfStrMapping.mapRoot(listOfStr, instanceRepository, typeProviders.parameterizedFlowProvider);
        logger.info(listOfStrView.getTitle());
    }

    public void testApplication() {
        var platformUserType = ClassTypeBuilder.newBuilder("平台用户", "PlatformUser").build();
        var loginNameField = FieldBuilder.newBuilder("登录名", "loginName", platformUserType, getStringType()).asTitle().build();

        var applicationType = ClassTypeBuilder.newBuilder("应用", "Application").build();
        var nameField = FieldBuilder.newBuilder("名称", "name", applicationType, getStringType()).asTitle().build();
        var ownerField = FieldBuilder.newBuilder("所有人", "owner", applicationType, platformUserType)
                .access(Access.PRIVATE).build();
        // create getOwner method
        var getOwnerMethod = MethodBuilder.newBuilder(applicationType, "获取所有人", "getOwner", typeProviders.functionTypeProvider)
                .returnType(platformUserType)
                .build();
        {
            var scope = getOwnerMethod.getRootScope();
            var selfNode = Nodes.self("Self", null, applicationType, scope);
            Nodes.ret("Return", scope, Values.nodeProperty(selfNode, ownerField));
        }
        TestUtils.initEntityIds(applicationType);

        var typeRepository = new MockTypeRepository();
        typeRepository.save(platformUserType);
        typeRepository.save(applicationType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                typeProviders.createFacade(),
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                mappingProvider,
                typeProviders.entityRepository
        );
        var applicationMapping = saver.saveBuiltinMapping(applicationType, true);
        System.out.println(applicationMapping.getReadMethod().getText());
    }

}
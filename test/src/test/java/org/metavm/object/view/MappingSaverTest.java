package org.metavm.object.view;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.entity.natives.mocks.MockNativeFunctionsInitializer;
import org.metavm.flow.*;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.mocks.MockInstanceRepository;
import org.metavm.object.type.*;
import org.metavm.object.type.mocks.MockTypeDefRepository;
import org.metavm.object.type.mocks.TypeProviders;
import org.metavm.object.view.mocks.MockMappingRepository;
import org.metavm.util.Instances;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.metavm.object.type.Types.getStringType;

public class MappingSaverTest extends TestCase {

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(MappingSaverTest.class);

    private InstanceRepository instanceRepository;
    private TypeProviders typeProviders;
    private EntityRepository entityRepository;
    private CallContext callContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instanceRepository = new MockInstanceRepository();
        typeProviders = new TypeProviders();
        entityRepository = typeProviders.entityRepository;
        callContext = new DefaultCallContext(instanceRepository);
        MockStandardTypesInitializer.init();
        MockNativeFunctionsInitializer.init();
    }

    public void testFromView() {
        var typeDefRepository = new MockTypeDefRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeDefRepository,
                mappingProvider,
                entityRepository
        );
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo")
                .build();
        FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .asTitle()
                .build();
        TestUtils.initEntityIds(fooType);
        var mapping = saver.saveBuiltinMapping(fooType, true);
        var fromViewMethod = MethodBuilder.newBuilder(fooType, "fromView", "fromView")
                .parameters(new Parameter(null, "view", "view", mapping.getTargetType()))
                .returnType(fooType.getType())
                .build();
        TestUtils.initEntityIds(fromViewMethod);
        saver.saveBuiltinMapping(fooType, true);
        logger.info(mapping.getUnmapper().getText());
    }

    public void testSaveBuiltin() {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo")
                .build();
        var barType = TestUtils.newKlassBuilder("Bar", "Bar")
                .build();

        var barChildArrayType = new ArrayType(barType.getType(), ArrayKind.CHILD);
        var barReadWriteArrayType = new ArrayType(barType.getType(), ArrayKind.READ_WRITE);

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
        var getBarsMethod = MethodBuilder.newBuilder(fooType, "getBars", "getBars")
                .returnType(barReadWriteArrayType)
                .build();
        {
            var scope = getBarsMethod.getScope();
            var barsNode = Nodes.newArray("newArray", null, barReadWriteArrayType,
                    Values.node(Nodes.thisProperty(fooBarsField, scope)), null, scope);
            Nodes.ret("return", scope, Values.node(barsNode));
        }

        // generate setBars method
        var setBarsMethod = MethodBuilder.newBuilder(fooType, "setBars", "setBars")
                .parameters(new Parameter(null, "bars", "bars", barReadWriteArrayType))
                .build();
        {
            var scope = setBarsMethod.getScope();
            Nodes.clearArray("clearBars", null, Values.node(Nodes.thisProperty(fooBarsField, scope)), scope);
            var bars = Nodes.argument(setBarsMethod, 0);
            Nodes.forEach(Values.node(bars),
                    (bodyScope, element, index) -> {
                        Nodes.addElement("addBar", null, Values.node(Nodes.thisProperty(fooBarsField, scope)),
                                element, bodyScope);
                    },
                    scope);
            Nodes.ret("return", scope, null);
        }
        TestUtils.initEntityIds(fooType);
        fooType.accept(new MaxesComputer());
        barType.accept(new MaxesComputer());

        var typeDefRepository = new MockTypeDefRepository();
        typeDefRepository.save(fooType);
        typeDefRepository.save(barType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeDefRepository,
                mappingProvider,
                entityRepository
        );

        var barMapping = saver.saveBuiltinMapping(barType, true);
        var fooMapping = saver.saveBuiltinMapping(fooType, true);
        var fooViewType = fooMapping.getTargetKlass();
        var barsFieldMapping = fooMapping.getFieldMappingByTargetField(fooViewType.getFieldByCode("bars"));
//        var barArrayMapping = Objects.requireNonNull(barsFieldMapping.getNestedMapping());
//        fooMapping.initId(3001L);
//        barArrayMapping.initId(3002L);
//        barMapping.initId(3003L);

        var barInst = ClassInstanceBuilder.newBuilder(barType.getType())
                .data(Map.of(
                        barCodeField,
                        new StringValue("bar001", Types.getStringType())
                ))
                .build();

        var barChildArray = new ArrayInstance(barChildArrayType, List.of(barInst.getReference()));

        var foo = ClassInstanceBuilder.newBuilder(fooType.getType())
                .data(
                        Map.of(
                                fooNameField,
                                new StringValue("foo", Types.getStringType()),
                                fooBarsField,
                                barChildArray.getReference()
                        )
                )
                .build();

        TestUtils.initInstanceIds(foo);

//        logger.info(barArrayMapping.getMapper().getText());
//        logger.info(barArrayMapping.getUnmapper().getText());

        TestUtils.initEntityIds(fooType);

        // Mapping
        var fooView = (ClassInstance) fooMapping.mapRoot(foo, callContext);

        Assert.assertSame(fooView.getKlass(), fooViewType);
        Assert.assertEquals(foo.getReference(), fooView.tryGetSource());
        var fooViewName = fooView.getField("name");
        Assert.assertEquals(foo.getField("name"), fooViewName);
        Assert.assertTrue(fooView.tryGetId() instanceof ViewId);

        var fooViewBars = fooView.getField("bars").resolveArray();
        Assert.assertTrue(fooViewBars.isChildArray());
        var barView = fooViewBars.get(0).resolveObject();
        var barViewType = barMapping.getTargetType();
        Assert.assertEquals(barView.getType(), barViewType);
        Assert.assertEquals(barInst.getField("code"), barView.getField("code"));
        Assert.assertTrue(barView.tryGetId() instanceof ChildViewId);

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
                fooMapping.unmap(fooView.getReference(), new DefaultCallContext(instanceRepository));
        Assert.assertSame(foo, unmappedFoo.resolve());
        Assert.assertEquals(Instances.stringInstance("foo2"), foo.getField("name"));
        var bars = foo.getField("bars").resolveArray();
        Assert.assertEquals(1, bars.size());
        var bar = bars.get(0).resolveObject();
        Assert.assertEquals(Instances.stringInstance("bar002"), bar.getField("code"));
    }

    public void testOrder() {
        var shoppingTypes = MockUtils.createShoppingTypes();
        TestUtils.initEntityIds(shoppingTypes.orderType());
        typeProviders.addTypeDef(shoppingTypes.orderType());
        typeProviders.addTypeDef(shoppingTypes.productType());
        typeProviders.addTypeDef(shoppingTypes.skuType());
        typeProviders.addTypeDef(shoppingTypes.couponType());
        typeProviders.addTypeDef(shoppingTypes.couponStateType());
//        typeProviders.addTypeDef(shoppingTypes.couponArrayType());
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeProviders.typeDefRepository,
                mappingProvider,
                typeProviders.entityRepository
        );
        var orderMapping = (FieldsObjectMapping) saver.saveBuiltinMapping(shoppingTypes.orderType(), true);
        TestUtils.initEntityIds(shoppingTypes.orderType());

        var order = ClassInstanceBuilder.newBuilder(shoppingTypes.orderType().getType())
                .data(Map.of(
                        shoppingTypes.orderAmountField(),
                        Instances.longInstance(1L),
                        shoppingTypes.orderCodeField(),
                        Instances.stringInstance("001"),
                        shoppingTypes.orderCouponsField(),
                        new ArrayInstance(
                                shoppingTypes.couponArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                                                .data(Map.of(
                                                        shoppingTypes.couponTitleField(),
                                                        Instances.stringInstance("Shoes reduced by 5 Yuan"),
                                                        shoppingTypes.couponDiscountField(),
                                                        Instances.longInstance(5L),
                                                        shoppingTypes.couponStateField(),
                                                        shoppingTypes.couponNormalState().getReference()
                                                ))
                                                .buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                                                .data(Map.of(
                                                        shoppingTypes.couponTitleField(),
                                                        Instances.stringInstance("Shoes reduced by 10 Yuan"),
                                                        shoppingTypes.couponDiscountField(),
                                                        Instances.longInstance(10L),
                                                        shoppingTypes.couponStateField(),
                                                        shoppingTypes.couponNormalState().getReference()
                                                ))
                                                .buildAndGetReference()
                                )
                        ).getReference(),
                        shoppingTypes.orderPriceField(),
                        Instances.doubleInstance(85.0),
                        shoppingTypes.orderProductField(),
                        ClassInstanceBuilder.newBuilder(shoppingTypes.productType().getType())
                                .data(Map.of(
                                        shoppingTypes.productTitleField(),
                                        Instances.stringInstance("Shoes"),
                                        shoppingTypes.productSkuListField(),
                                        new ArrayInstance(
                                                shoppingTypes.skuChildArrayType(),
                                                List.of()
                                        ).getReference()
                                ))
                                .buildAndGetReference(),
                        shoppingTypes.orderTimeField(),
                        Instances.timeInstance(System.currentTimeMillis())
                ))
                .build();
        logger.info(orderMapping.getReadMethod().getText());
        logger.info(orderMapping.getMapper().getText());
        var oderView = orderMapping.mapRoot(order, new DefaultCallContext(instanceRepository));
    }

    public void testPathId() {
        var productType = TestUtils.newKlassBuilder("Product", "Product").build();
        var skuType = TestUtils.newKlassBuilder("Sku", "Sku").build();
        var skuChildArrayType = new ArrayType(skuType.getType(), ArrayKind.CHILD);
        var skuListField = FieldBuilder.newBuilder("skuList", "skuList", productType, skuChildArrayType)
                .isChild(true)
                .access(Access.PRIVATE)
                .build();
        var skuRwArrayType = new ArrayType(skuType.getType(), ArrayKind.READ_WRITE);
        var getSkuListMethod = MethodBuilder.newBuilder(productType, "getSkuList", "getSkuList")
                .returnType(skuRwArrayType)
                .build();
        {
            var scope = getSkuListMethod.getScope();
            var skuList = Nodes.newArray(
                    "skuList", "skuList", skuRwArrayType,
                    Values.node(Nodes.thisProperty(skuListField, scope)),
                    null, scope
            );
            Nodes.ret("return", scope, Values.node(skuList));
        }

        var setSkuListMethod = MethodBuilder.newBuilder(productType, "setSkuList", "setSkuList")
                .parameters(new Parameter(null, "skuList", "skuList", skuRwArrayType))
                .build();
        {
            var scope = setSkuListMethod.getScope();
            Nodes.clearArray("clearArray", null, Values.node(Nodes.thisProperty(skuListField, scope)), scope);
            var array = Nodes.argument(setSkuListMethod, 0);
            Nodes.forEach(
                     Values.node(array),
                    (bodyScope, element, index) -> {
                        Nodes.addElement("addElement", null,
                                Values.node(Nodes.thisProperty(skuListField, scope)), element, bodyScope);
                    },
                    scope
            );
        }
        TestUtils.initEntityIds(productType);
        productType.accept(new MaxesComputer());
        var typeDefRepository = new MockTypeDefRepository();
        typeDefRepository.save(productType);
        typeDefRepository.save(skuType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeDefRepository,
                mappingProvider,
                typeProviders.entityRepository
        );
        saver.saveBuiltinMapping(skuType, true);
        var productMapping = saver.saveBuiltinMapping(productType, true);
        TestUtils.initEntityIds(productType);
        var sku = ClassInstanceBuilder.newBuilder(skuType.getType())
                .data(Map.of())
                .build();
        var product = ClassInstanceBuilder.newBuilder(productType.getType())
                .data(Map.of(
                        skuListField,
                        new ArrayInstance(skuChildArrayType, List.of(sku.getReference())).getReference()
                ))
                .build();
        TestUtils.initInstanceIds(product);
        var viewSkuListField = productMapping.getTargetKlass().getFieldByCode("skuList");
        var productView = (ClassInstance) productMapping.mapRoot(product, callContext);
        var skuListView = productView.getField(viewSkuListField).resolveArray();
        Assert.assertTrue(productView.tryGetId() instanceof ViewId);
        Assert.assertTrue(skuListView.tryGetId() instanceof FieldViewId);
        Assert.assertTrue(skuListView.get(0).tryGetId() instanceof ChildViewId);

        skuListView.removeElement(0);

        productMapping.unmap(productView.getReference(), callContext);
        var skuList = product.getField(skuListField).resolveArray();
        Assert.assertTrue(skuList.isEmpty());
    }

    public void testUnionType() {
        var flowType = TestUtils.newKlassBuilder("Flow", "Flow").build();
        var scopeType = TestUtils.newKlassBuilder("Scope", "Scope").build();
        var scopeArrayType = new ArrayType(scopeType.getType(), ArrayKind.CHILD);
        var nullableScopeArrayType = new UnionType(Set.of(scopeArrayType, Types.getNullType()));
        var flowScopesField = FieldBuilder.newBuilder("scopes", "scopes", flowType, nullableScopeArrayType).isChild(true).build();
        TestUtils.initEntityIds(flowType);
        var typeRepository = new MockTypeDefRepository();
        typeRepository.save(flowType);
        typeRepository.save(scopeType);
//        typeRepository.save(scopeArrayType);
//        typeRepository.save(nullableScopeArrayType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                mappingProvider,
                typeProviders.entityRepository
        );
        var scopeMapping = saver.saveBuiltinMapping(scopeType, true);
        var flowMapping = saver.saveBuiltinMapping(flowType, true);
        var flowViewType = flowMapping.getTargetKlass();
        var flowViewScopesField = flowViewType.getFieldByCode("scopes");

        TestUtils.initEntityIds(flowType);

//        logger.info(flowMapping.getReadMethod().getText());
//        logger.info(flowMapping.getMapper().getText());
//        logger.info(flowMapping.getWriteMethod().getText());
//        logger.info(flowMapping.getUnmapper().getText());

        var flow = ClassInstanceBuilder.newBuilder(flowType.getType())
                .data(Map.of(
                        flowScopesField,
                        new ArrayInstance(
                                scopeArrayType,
                                List.of(
                                        ClassInstanceBuilder.newBuilder(scopeType.getType()).buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(scopeType.getType()).buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(scopeType.getType()).buildAndGetReference()
                                )
                        ).getReference()
                ))
                .build();
        TestUtils.initInstanceIds(flow);

        var flowView = (ClassInstance) flowMapping.mapRoot(flow, callContext);
        var flowViewScopes = flowView.getField(flowViewScopesField).resolveArray();
        Assert.assertEquals(3, flowViewScopes.size());

        flowViewScopes.removeElement(2);
        flowMapping.unmap(flowView.getReference(), callContext);
        var flowScopes = flow.getField(flowScopesField).resolveArray();
        Assert.assertEquals(2, flowScopes.size());

        flowView.setField(flowViewScopesField, Instances.nullInstance());
        flowMapping.unmap(flowView.getReference(), callContext);
        Assert.assertTrue(flow.getField(flowScopesField).isNull());
    }

    public void testGeneric() {
        var typeDefRepository = new MockTypeDefRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeDefRepository,
                mappingProvider,
                typeProviders.entityRepository
        );

        var nodeType = TestUtils.newKlassBuilder("Node", "Node")
                .typeParameters(new TypeVariable(null, "V", "V", DummyGenericDeclaration.INSTANCE))
                .build();
        FieldBuilder.newBuilder("label", "label", nodeType, getStringType())
                .asTitle()
                .build();
        FieldBuilder.newBuilder("value", "value", nodeType, nodeType.getTypeParameters().get(0).getType())
                .build();

        var nodeMapping = saver.saveBuiltinMapping(nodeType, true);
        var listTypeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);
        var listKlass = TestUtils.newKlassBuilder("List", "List")
                .typeParameters(listTypeVar)
                .build();
        var pNodeType = nodeType.getParameterized(List.of(listTypeVar.getType()), ResolutionStage.DEFINITION);
        var pNodeChildArrayType = new ArrayType(pNodeType.getType(), ArrayKind.CHILD);
        var listNodeField = FieldBuilder.newBuilder("nodes", "nodes", listKlass, pNodeChildArrayType).isChild(true).build();

        TestUtils.initEntityIds(listKlass);

        typeDefRepository.save(listKlass);

        var listMapping = saver.saveBuiltinMapping(listKlass, true);

        var listOfStrType = listKlass.getParameterized(List.of(Types.getStringType()), ResolutionStage.DEFINITION);
        TestUtils.initEntityIds(listOfStrType);

        var listOfStrMapping = Objects.requireNonNull(listOfStrType.getDefaultMapping());
        var listOfStrNodesField = listOfStrType.getFieldByCode("nodes");
        var nodeOfStrChildArrayType = (ArrayType) listOfStrNodesField.getType();
        var nodeOfStrType = ((ClassType) nodeOfStrChildArrayType.getElementType()).resolve();
        var listOfStr = ClassInstanceBuilder.newBuilder(listOfStrType.getType())
                .data(Map.of(
                        listOfStrNodesField,
                        new ArrayInstance(
                                nodeOfStrChildArrayType,
                                List.of(
                                        ClassInstanceBuilder.newBuilder(nodeOfStrType.getType())
                                                .data(
                                                        Map.of(
                                                                nodeOfStrType.getFieldByCode("label"),
                                                                Instances.stringInstance("node001"),
                                                                nodeOfStrType.getFieldByCode("value"),
                                                                Instances.stringInstance("Hello")
                                                        )
                                                )
                                                .buildAndGetReference()
                                )
                        ).getReference()
                ))
                .build();
        TestUtils.initInstanceIds(listOfStr);
        var listOfStrView = listOfStrMapping.mapRoot(listOfStr, callContext);
        logger.info(listOfStrView.getTitle());
    }

    public void testApplication() {
        var platformUserType = TestUtils.newKlassBuilder("PlatformUser", "PlatformUser").build();
        var loginNameField = FieldBuilder.newBuilder("loginName", "loginName", platformUserType, getStringType()).asTitle().build();

        var applicationType = TestUtils.newKlassBuilder("Application", "Application").build();
        var nameField = FieldBuilder.newBuilder("name", "name", applicationType, getStringType()).asTitle().build();
        var ownerField = FieldBuilder.newBuilder("owner", "owner", applicationType, platformUserType.getType())
                .access(Access.PRIVATE).build();
        // create getOwner method
        var getOwnerMethod = MethodBuilder.newBuilder(applicationType, "getOwner", "getOwner")
                .returnType(platformUserType.getType())
                .build();
        {
            var scope = getOwnerMethod.getScope();
            Nodes.ret("return", scope, Values.node(Nodes.thisProperty(ownerField, scope)));
        }
        TestUtils.initEntityIds(applicationType);

        var typeDefRepository = new MockTypeDefRepository();
        typeDefRepository.save(platformUserType);
        typeDefRepository.save(applicationType);
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeDefRepository,
                mappingProvider,
                typeProviders.entityRepository
        );
        var applicationMapping = saver.saveBuiltinMapping(applicationType, true);
        System.out.println(applicationMapping.getReadMethod().getText());
    }

}
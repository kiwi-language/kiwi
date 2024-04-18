package tech.metavm.util;

import tech.metavm.asm.AssemblerFactory;
import tech.metavm.entity.MemTypeRegistry;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;
import tech.metavm.object.type.rest.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.metavm.entity.StandardTypes.*;

public class MockUtils {

    public static ShoppingTypes createShoppingTypes() {
        var productType = ClassTypeBuilder.newBuilder("商品", "Product").build();
        var skuType = ClassTypeBuilder.newBuilder("SKU", "SKU").build();
        var couponType = ClassTypeBuilder.newBuilder("优惠券", "Coupon").build();
        var couponArrayType = new ArrayType(null, couponType, ArrayKind.READ_WRITE);
        var orderType = ClassTypeBuilder.newBuilder("订单", "Order").build();
        var couponStateType = ClassTypeBuilder.newBuilder("优惠券状态", "CouponState")
                .category(TypeCategory.ENUM)
                .build();
        var enumType = getEnumType();
        var subst = new SubstitutorV2(
                enumType, enumType.getTypeParameters(), List.of(couponStateType),
                ResolutionStage.DEFINITION,
                new MockEntityRepository(new MemTypeRegistry()),
                new UnsupportedCompositeTypeFacade(),
                new UnsupportedParameterizedTypeRepository(),
                new UnsupportedParameterizedFlowProvider(),
                new MockDTOProvider()
        );
        var couponStateEnumType = (ClassType) subst.visitClassType(enumType);
        couponStateType.setSuperClass(couponStateEnumType);
        var enumNameField = couponStateEnumType.getFieldByCode("name");
        var enumOrdinalField = couponStateEnumType.getFieldByCode("ordinal");
        var couponNormalState = ClassInstanceBuilder.newBuilder(couponStateType)
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("正常"),
                        enumOrdinalField,
                        Instances.longInstance(0L)
                ))
                .id(TmpId.of(NncUtils.randomNonNegative()))
                .build();
        var couponUsedState = ClassInstanceBuilder.newBuilder(couponStateType)
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("已使用"),
                        enumOrdinalField,
                        Instances.longInstance(1L)
                ))
                .id(TmpId.of(NncUtils.randomNonNegative()))
                .build();
        createEnumConstantField(couponNormalState);
        createEnumConstantField(couponUsedState);
        var productTitleField = FieldBuilder.newBuilder("标题", "title", productType, getStringType())
                .asTitle()
                .build();
        var skuChildArrayType = new ArrayType(null, skuType, ArrayKind.CHILD);
        var productSkuListField = FieldBuilder.newBuilder("sku列表", "skuList", productType, skuChildArrayType)
                .isChild(true)
                .build();
        var skuTitleField = FieldBuilder.newBuilder("标题", "title", skuType, getStringType())
                .asTitle()
                .build();
        var skuPriceField = FieldBuilder.newBuilder("价格", "price", skuType, getDoubleType())
                .build();
        var skuAmountField = FieldBuilder.newBuilder("数量", "amount", skuType, getLongType())
                .access(Access.PRIVATE)
                .build();
        var orderCodeField = FieldBuilder.newBuilder("编号", "code", orderType, getStringType())
                .asTitle()
                .build();
        var orderProductField = FieldBuilder.newBuilder("商品", "product", orderType, productType).build();
        var orderCouponsField = FieldBuilder.newBuilder("优惠券列表", "coupons", orderType, couponArrayType)
                .isChild(true).build();
        var orderAmountField = FieldBuilder.newBuilder("数量", "amount", orderType, getLongType()).build();
        var orderPriceField = FieldBuilder.newBuilder("价格", "price", orderType, getDoubleType()).build();
        var orderTimeField = FieldBuilder.newBuilder("时间", "time", orderType, getTimeType()).build();
        var couponTitleField = FieldBuilder.newBuilder("标题", "title", couponType, getStringType())
                .asTitle()
                .build();
        var couponDiscountField = FieldBuilder.newBuilder("折扣", "discount", couponType, getDoubleType())
                .build();
        var couponStateField = FieldBuilder.newBuilder("状态", "state", couponType, couponStateType)
                .defaultValue(couponNormalState)
                .build();

        return new ShoppingTypes(
                productType, skuType, couponType, orderType, couponStateType, skuChildArrayType, couponArrayType,
                productTitleField, productSkuListField, skuTitleField, skuPriceField, skuAmountField,
                couponTitleField, couponDiscountField, couponStateField, orderCodeField, orderProductField,
                orderCouponsField, orderAmountField, orderPriceField, orderTimeField, couponNormalState, couponUsedState
        );
    }

    public static InstanceDTO createProductDTO(InstanceManager instanceManager, ShoppingTypeIds shoppingTypeIds) {
        var productId = TestUtils.doInTransaction(() -> instanceManager.create(createProductDTO(shoppingTypeIds)));
        return instanceManager.get(productId, 1).instance();
    }

    public static List<InstanceDTO> createCouponDTOs(InstanceManager instanceManager, ShoppingTypeIds shoppingTypeIds) {
        var couponFiveOff = new InstanceDTO(
                null,
                shoppingTypeIds.couponTypeId(),
                "优惠券",
                "减5元",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("减5元")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponDiscountFieldId(),
                                        PrimitiveFieldValue.createDouble(5.0)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponStateFieldId(),
                                        ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId())
                                )
                        )
                )
        );
        var couponTenOff = new InstanceDTO(
                null,
                shoppingTypeIds.couponTypeId(),
                "优惠券",
                "减10元",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("减10元")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponDiscountFieldId(),
                                        PrimitiveFieldValue.createDouble(10.0)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponStateFieldId(),
                                        ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId())
                                )
                        )
                )
        );
        var couponFifteenOff = new InstanceDTO(
                null,
                shoppingTypeIds.couponTypeId(),
                "优惠券",
                "减15元",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("减15元")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponDiscountFieldId(),
                                        PrimitiveFieldValue.createDouble(15.0)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponStateFieldId(),
                                        ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId())
                                )
                        )
                )
        );
        var couponFiveOffId = TestUtils.doInTransaction(() -> instanceManager.create(couponFiveOff));
        var couponTenOffId = TestUtils.doInTransaction(() -> instanceManager.create(couponTenOff));
        var couponFifteenOffId = TestUtils.doInTransaction(() -> instanceManager.create(couponFifteenOff));
        return List.of(
                instanceManager.get(couponFiveOffId, 1).instance(),
                instanceManager.get(couponTenOffId, 1).instance(),
                instanceManager.get(couponFifteenOffId, 1).instance()
        );
    }

    public static InstanceDTO createProductDTO(ShoppingTypeIds shoppingTypeIds) {
        return new InstanceDTO(
                null,
                shoppingTypeIds.productTypeId(),
                "商品",
                "鞋子",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.productTitleFieldId(),
                                        PrimitiveFieldValue.createString("鞋子")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.productSkuListFieldId(),
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        shoppingTypeIds.skuChildArrayTypeId(),
                                                        true,
                                                        List.of(
                                                                InstanceFieldValue.of(
                                                                        new InstanceDTO(
                                                                                null,
                                                                                shoppingTypeIds.skuTypeId(),
                                                                                "SKU",
                                                                                "40",
                                                                                null,
                                                                                new ClassInstanceParam(
                                                                                        List.of(
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuTitleFieldId(),
                                                                                                        PrimitiveFieldValue.createString("40")
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuPriceFieldId(),
                                                                                                        PrimitiveFieldValue.createDouble(100)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuAmountFieldId(),
                                                                                                        PrimitiveFieldValue.createLong(100)
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )),
                                                                InstanceFieldValue.of(new InstanceDTO(
                                                                        null,
                                                                        shoppingTypeIds.skuTypeId(),
                                                                        "SKU",
                                                                        "41",
                                                                        null,
                                                                        new ClassInstanceParam(
                                                                                List.of(
                                                                                        InstanceFieldDTO.create(
                                                                                                shoppingTypeIds.skuTitleFieldId(),
                                                                                                PrimitiveFieldValue.createString("41")
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                shoppingTypeIds.skuPriceFieldId(),
                                                                                                PrimitiveFieldValue.createDouble(100)
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                shoppingTypeIds.skuAmountFieldId(),
                                                                                                PrimitiveFieldValue.createLong(100)
                                                                                        )
                                                                                )
                                                                        )
                                                                ))
                                                                ,
                                                                InstanceFieldValue.of(new InstanceDTO(
                                                                                null,
                                                                                shoppingTypeIds.skuTypeId(),
                                                                                "SKU",
                                                                                "42",
                                                                                null,
                                                                                new ClassInstanceParam(
                                                                                        List.of(
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuTitleFieldId(),
                                                                                                        PrimitiveFieldValue.createString("42")
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuPriceFieldId(),
                                                                                                        PrimitiveFieldValue.createDouble(100)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        shoppingTypeIds.skuAmountFieldId(),
                                                                                                        PrimitiveFieldValue.createLong(100)
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

    }

    public static ShoppingInstances createShoppingInstances(ShoppingTypes shoppingTypes) {
        var sku40 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("40"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku41 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("41"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku42 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("42"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var product = ClassInstanceBuilder.newBuilder(shoppingTypes.productType())
                .data(Map.of(
                        shoppingTypes.productTitleField(),
                        Instances.stringInstance("鞋子"),
                        shoppingTypes.productSkuListField(),
                        new ArrayInstance(shoppingTypes.skuChildArrayType(),
                                List.of(sku40, sku41, sku42))
                ))
                .build();
        var couponFiveOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("减5元"),
                        shoppingTypes.couponDiscountField(),
                        Instances.longInstance(5L)
                ))
                .build();
        var couponTenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("减10元"),
                        shoppingTypes.couponDiscountField(),
                        Instances.longInstance(10L)
                ))
                .build();
        var couponFifteenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("减15元"),
                        shoppingTypes.couponDiscountField(),
                        Instances.longInstance(15L)
                ))
                .build();
        return new ShoppingInstances(
                product,
                sku40,
                sku41,
                sku42,
                couponFiveOff,
                couponTenOff,
                couponFifteenOff
        );
    }

    public static final String PROJECT_ROOT = "/Users/leen/workspace/object";

    public static ListTypeIds createListType(TypeManager typeManager) {
        saveListTypes(typeManager);
        var listType = typeManager.getTypeByCode("MyList").type();
        return new ListTypeIds(
                listType.id(),
                listType.typeParameterIds().get(0),
                TestUtils.getFieldIdByCode(listType, "label"),
                TestUtils.getFieldIdByCode(listType, "nodes"),
                getNodeTypeIds(typeManager)
        );
    }

    public static NodeTypeIds createNodeTypes(TypeManager typeManager) {
        saveListTypes(typeManager);
        return getNodeTypeIds(typeManager);
    }

    private static void saveListTypes(TypeManager typeManager) {
        assemble(PROJECT_ROOT + "/test/src/test/resources/asm/List.masm", typeManager);
    }

    private static NodeTypeIds getNodeTypeIds(TypeManager typeManager) {
        var nodeType = typeManager.getTypeByCode("Node").type();
        return new NodeTypeIds(
                nodeType.id(),
                nodeType.typeParameterIds().get(0),
                TestUtils.getFieldIdByCode(nodeType, "label"),
                TestUtils.getFieldIdByCode(nodeType, "value")
        );
    }

    private static Field createEnumConstantField(ClassInstance enumConstant) {
        var enumType = enumConstant.getType();
        var nameField = enumType.getFieldByCode("name");
        var name = enumConstant.getStringField(nameField).getValue();
        return FieldBuilder.newBuilder(name, null, enumType, enumType)
                .isStatic(true)
                .staticValue(enumConstant)
                .build();
    }

    private static TypeDTO saveType(TypeManager typeManager, TypeDTO typeDTO) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.saveType(typeDTO));
    }

    private static List<String> batchSaveTypes(TypeManager typeManager, List<TypeDTO> typeDTOs) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(typeDTOs, List.of(), List.of(), false)
        ));
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm", typeManager);
        var productType = typeManager.getTypeByCode("Product").type();
        var skuType = typeManager.getTypeByCode("SKU").type();
        var couponType = typeManager.getTypeByCode("Coupon").type();
        var couponStateType = typeManager.getTypeByCode("CouponState").type();
        var orderType = typeManager.getTypeByCode("Order").type();
        var skuChildListType = typeManager.getParameterizedType(GetParameterizedTypeRequest.create(
              StandardTypes.getChildListType().getStringId(), List.of(skuType.id())
        )).type();
        var couponListType = typeManager.getParameterizedType(GetParameterizedTypeRequest.create(
                StandardTypes.getReadWriteListType().getStringId(),
                List.of(couponType.id())
        )).type();
        return new ShoppingTypeIds(
                productType.id(),
                skuType.id(),
                couponStateType.id(),
                couponType.id(),
                orderType.id(),
                skuChildListType.id(),
                couponListType.id(),
                TestUtils.getFieldIdByCode(productType, "name"),
                TestUtils.getFieldIdByCode(productType, "skuList"),
                TestUtils.getFieldIdByCode(skuType, "name"),
                TestUtils.getFieldIdByCode(skuType, "price"),
                TestUtils.getFieldIdByCode(skuType, "quantity"),
                TestUtils.getMethodIdByCode(skuType, "decQuantity"),
                TestUtils.getMethodIdByCode(skuType, "buy"),
                TestUtils.getFieldIdByCode(couponType, "name"),
                TestUtils.getFieldIdByCode(couponType, "discount"),
                TestUtils.getFieldIdByCode(couponType, "state"),
                TestUtils.getFieldIdByCode(orderType, "code"),
                TestUtils.getFieldIdByCode(orderType, "sku"),
                TestUtils.getFieldIdByCode(orderType, "quantity"),
                TestUtils.getFieldIdByCode(orderType, "price"),
                TestUtils.getFieldIdByCode(orderType, "orderTime"),
                TestUtils.getFieldIdByCode(orderType, "coupons"),
                TestUtils.getEnumConstantIdByName(couponStateType, "NORMAL"),
                TestUtils.getEnumConstantIdByName(couponStateType, "USED")
        );
    }

    public static LivingBeingTypeIds createLivingBeingTypes(TypeManager typeManager) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm", typeManager);
        var livingBeingType = typeManager.getTypeByCode("LivingBeing").type();
        var animalType = typeManager.getTypeByCode("Animal").type();
        var humanType = typeManager.getTypeByCode("Human").type();
        var sentientType = typeManager.getTypeByCode("Sentient").type();
        return new LivingBeingTypeIds(
                livingBeingType.id(),
                animalType.id(),
                humanType.id(),
                TestUtils.getFieldIdByCode(livingBeingType, "age"),
                TestUtils.getFieldIdByCode(livingBeingType, "extra"),
                TestUtils.getFieldIdByCode(livingBeingType, "offsprings"),
                TestUtils.getFieldIdByCode(livingBeingType, "ancestors"),
                TestUtils.getFieldIdByCode(animalType, "intelligence"),
                TestUtils.getFieldIdByCode(humanType, "occupation"),
                TestUtils.getFieldIdByCode(humanType, "thinking"),
                TestUtils.getMethodIdByCode(livingBeingType, "LivingBeing"),
                TestUtils.getMethodIdByCode(animalType, "Animal"),
                TestUtils.getMethodIdByCode(humanType, "Human"),
                TestUtils.getMethodIdByCode(livingBeingType, "makeSound"),
                TestUtils.getMethodIdByCode(sentientType, "think")
        );
    }

    public static UtilsTypeIds createUtilsTypes(TypeManager typeManager) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm", typeManager);
        var utilsType = typeManager.getTypeByCode("Utils").type();
        return new UtilsTypeIds(
                utilsType.id(),
                TestUtils.getStaticMethodIdByCode(utilsType, "containsAny"),
                TestUtils.getStaticMethodIdByCode(utilsType, "test")
        );
    }

    public static void assemble(String source, TypeManager typeManager) {
        var assembler = AssemblerFactory.createWithStandardTypes();
        assembler.assemble(List.of(source));
        FlowSavingContext.initConfig();
        TestUtils.doInTransaction(() -> typeManager.batchSave(new BatchSaveRequest(assembler.getAllTypes(), List.of(), assembler.getParameterizedFlows(), false)));
    }

    public static FooTypes createFooTypes() {
        return createFooTypes(false);
    }

    public static FooTypes createFooTypes(boolean initIds) {
        var fooType = ClassTypeBuilder.newBuilder("傻", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("名称", "name", fooType, getStringType())
                .asTitle().build();
        var fooCodeField = FieldBuilder.newBuilder("编号", "code", fooType, getNullableStringType())
                .build();
        var barType = ClassTypeBuilder.newBuilder("巴", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("编号", "code", barType, getStringType())
                .asTitle().build();
        var barChildArrayType = new ArrayType(null, barType, ArrayKind.CHILD);
        var barArrayType = new ArrayType(null, barType, ArrayKind.READ_WRITE);
//        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("巴列表", "bars", fooType, barChildArrayType)
                .isChild(true).build();
        var bazType = ClassTypeBuilder.newBuilder("巴子", "Baz").build();
        var bazArrayType = new ArrayType(null, bazType, ArrayKind.READ_WRITE);
        var bazBarsField = FieldBuilder.newBuilder("巴列表", "bars", bazType, barArrayType).build();
        var fooBazListField = FieldBuilder.newBuilder("巴子列表", "bazList", fooType, bazArrayType).build();
        var quxType = ClassTypeBuilder.newBuilder("量子", "Qux").build();
        var quxAmountField = FieldBuilder.newBuilder("数量", "amount", quxType, getLongType()).build();
        var nullableQuxType = new UnionType(null, Set.of(quxType, getNullType()));
        var fooQuxField = FieldBuilder.newBuilder("量子", "qux", fooType, nullableQuxType).build();
        if (initIds)
            TestUtils.initEntityIds(fooType);
        return new FooTypes(fooType, barType, quxType, bazType, barArrayType, barChildArrayType, bazArrayType, fooNameField,
                fooCodeField, fooBarsField, fooQuxField, fooBazListField, barCodeField, bazBarsField, quxAmountField);
    }

    public static LivingBeingTypes createLivingBeingTypes(boolean initIds) {
        var livingBeingType = ClassTypeBuilder.newBuilder("生物", "LivingBeing").build();
        var livingBeingAgeField = FieldBuilder.newBuilder("年龄", "age", livingBeingType, getLongType())
                .build();
        var livingBeingExtraInfoField = FieldBuilder.newBuilder("额外信息", "extraInfo", livingBeingType, getAnyType())
                .build();
        var livingBeingArrayType = new ArrayType(null, livingBeingType, ArrayKind.READ_WRITE);
        var livingBeingOffspringsField = FieldBuilder.newBuilder("后代", "offsprings", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var livingBeingAncestorsField = FieldBuilder.newBuilder("祖先", "ancestors", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var animalType = ClassTypeBuilder.newBuilder("动物", "Animal")
                .superClass(livingBeingType)
                .build();
        var animalIntelligenceField = FieldBuilder.newBuilder("智力", "intelligence", animalType, getLongType())
                .build();
        var humanType = ClassTypeBuilder.newBuilder("人类", "Human")
                .superClass(animalType)
                .build();
        var humanOccupationField = FieldBuilder.newBuilder("职业", "occupation", humanType, getStringType())
                .build();
        if (initIds)
            TestUtils.initEntityIds(livingBeingType);
        return new LivingBeingTypes(
                livingBeingType,
                animalType,
                humanType,
                livingBeingArrayType,
                livingBeingAgeField,
                livingBeingExtraInfoField,
                livingBeingOffspringsField,
                livingBeingAncestorsField,
                animalIntelligenceField,
                humanOccupationField
        );
    }

    public static ClassInstance createHuman(LivingBeingTypes livingBeingTypes, boolean initIds) {
        var human = ClassInstanceBuilder.newBuilder(livingBeingTypes.humanType())
                .data(Map.of(
                        livingBeingTypes.livingBeingAgeField(),
                        Instances.longInstance(30L),
                        livingBeingTypes.livingBeingAncestorsField(),
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()),
                        livingBeingTypes.livingBeingOffspringsField(),
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()),
                        livingBeingTypes.livingBeingExtraInfoFIeld(),
                        Instances.stringInstance("非常聪明"),
                        livingBeingTypes.animalIntelligenceField(),
                        Instances.longInstance(160L),
                        livingBeingTypes.humanOccupationField(),
                        Instances.stringInstance("程序员")
                ))
                .build();
        if (initIds)
            TestUtils.initInstanceIds(human);
        return human;
    }

    public static ClassInstance createFoo(FooTypes fooTypes) {
        return createFoo(fooTypes, false);
    }

    public static ClassInstance createFoo(FooTypes fooTypes, boolean initIds) {
        var foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType())
                .data(Map.of(
                        fooTypes.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(
                                fooTypes.barChildArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar001")
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar002")
                                                ))
                                                .build()
                                )
                        ),
                        fooTypes.fooQuxField(),
                        ClassInstanceBuilder.newBuilder(fooTypes.quxType())
                                .data(
                                        Map.of(
                                                fooTypes.quxAmountField(),
                                                Instances.longInstance(100L)
                                        )
                                )
                                .build(),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(
                                fooTypes.bazArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar003")
                                                                                ))
                                                                                .build(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar004")
                                                                                ))
                                                                                .build()
                                                                )
                                                        )
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar005")
                                                                                ))
                                                                                .build(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar006")
                                                                                ))
                                                                                .build()
                                                                )
                                                        )
                                                ))
                                                .build()
                                )
                        )
                ))
                .build();
        if (initIds)
            TestUtils.initInstanceIds(foo);
        return foo;
    }

    public static UserTypeIds createUserTypes(TypeManager typeManager) {
        var platformUserTypeDTO = ClassTypeDTOBuilder.newBuilder("平台用户")
                .code("PlatformUser")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldId(TmpId.of(NncUtils.randomNonNegative()).toString())
                .addField(FieldDTOBuilder.newBuilder("登录名", getStringType().getStringId())
                        .code("loginName")
                        .asTitle(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("密码", getPasswordType().getStringId())
                        .code("password")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .build();
        var applicationTypeTmpId = TmpId.random().toString();
        var applicationTypeDTO = ClassTypeDTOBuilder.newBuilder("应用")
                .id(applicationTypeTmpId)
                .code("Application")
                .titleFieldId(TmpId.random().toString())
                .addField(FieldDTOBuilder.newBuilder("名称", getStringType().getStringId())
                        .code("name")
                        .asTitle(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("所有者", platformUserTypeDTO.id())
                        .code("owner")
                        .access(Access.PRIVATE.code())
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addMethod(MethodDTOBuilder.newBuilder(applicationTypeTmpId, "获取所有者")
                        .tmpId(NncUtils.randomNonNegative())
                        .code("getOwner")
                        .returnTypeId(platformUserTypeDTO.id())
                        .addNode(NodeDTOFactory.createSelfNode(
                                NncUtils.randomNonNegative(),
                                "当前记录",
                                applicationTypeTmpId
                        ))
                        .addNode(NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                "返回",
                                ValueDTOFactory.createReference("当前记录.所有者")
                        ))
                        .build())
                .build();
        var typeIds = TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(List.of(platformUserTypeDTO, applicationTypeDTO), List.of(), List.of(), false)
        ));
        var applicationType = typeManager.getType(new GetTypeRequest(typeIds.get(1), false)).type();
        var applicationNameFieldId = TestUtils.getFieldIdByCode(applicationType, "name");
        var applicationOwnerFieldId = TestUtils.getFieldIdByCode(applicationType, "owner");
        // get the longName field id and password field id of the platform user type
        var platformUserType = typeManager.getType(new GetTypeRequest(typeIds.get(0), false)).type();
        var platformUserLoginNameFieldId = TestUtils.getFieldIdByCode(platformUserType, "loginName");
        var platformUserPasswordFieldId = TestUtils.getFieldIdByCode(platformUserType, "password");
        return new UserTypeIds(typeIds.get(0), typeIds.get(1), applicationNameFieldId, applicationOwnerFieldId, platformUserLoginNameFieldId,
                platformUserPasswordFieldId);
    }

    public static Foo getFoo() {
        Foo foo = new Foo(
                "Big Foo",
                new Bar("Bar001")
        );
        foo.setCode("Foo001");

        foo.setQux(new Qux(100));
        Baz baz1 = new Baz();
        baz1.setBars(List.of(new Bar("Bar002")));
        Baz baz2 = new Baz();
        foo.setBazList(List.of(baz1, baz2));
        return foo;
    }
}

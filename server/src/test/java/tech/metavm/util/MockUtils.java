package tech.metavm.util;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;
import tech.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.metavm.entity.StandardTypes.*;

public class MockUtils {

    public static ShoppingTypes createShoppingTypes() {
        var productType = ClassTypeBuilder.newBuilder("商品", "Product").build();
        var skuType = ClassTypeBuilder.newBuilder("SKU", "SKU").build();
        var couponType = ClassTypeBuilder.newBuilder("优惠券", "Coupon").build();
        var orderType = ClassTypeBuilder.newBuilder("订单", "Order").build();
        var couponStateType = ClassTypeBuilder.newBuilder("优惠券状态", "CouponState")
                .category(TypeCategory.ENUM)
                .build();
        var enumType = getEnumType();
        var subst = new SubstitutorV2(
                enumType, enumType.getTypeParameters(), List.of(couponStateType),
                ResolutionStage.DEFINITION,
                new MockEntityRepository(),
                new UnsupportedCompositeTypeFacade(),
                new UnsupportedParameterizedTypeProvider(),
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
                productType, skuType, couponType, orderType, couponStateType, skuChildArrayType,
                productTitleField, productSkuListField, skuTitleField, skuPriceField, skuAmountField,
                couponTitleField, couponDiscountField, couponStateField, orderCodeField, orderProductField,
                orderAmountField, orderPriceField, orderTimeField
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
        TestUtils.beginTransaction();
        var result = typeManager.saveType(typeDTO);
        TestUtils.commitTransaction();
        ;
        return result;
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager) {
        long titleFieldTmpId = NncUtils.randomNonNegative();
        var skuTypeTmpId = NncUtils.randomNonNegative();
        var skuAmountFieldTmpId = NncUtils.randomNonNegative();
        FlowSavingContext.initConfig();
        var skuTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("SKU")
                .code("SKU")
                .tmpId(skuTypeTmpId)
                .titleFieldRef(RefDTO.fromTmpId(titleFieldTmpId))
                .addField(FieldDTOBuilder.newBuilder("标题", getStringType().getRef())
                        .code("title")
                        .tmpId(titleFieldTmpId)
                        .build())
                .addField(FieldDTOBuilder.newBuilder("价格", getDoubleType().getRef())
                        .code("price")
                        .tmpId(NncUtils.randomNonNegative())
                        .build())
                .addField(FieldDTOBuilder.newBuilder("库存", getLongType().getRef())
                        .code("amount")
                        .access(Access.PRIVATE.code())
                        .tmpId(skuAmountFieldTmpId)
                        .build())
                .addMethod(
                        MethodDTOBuilder.newBuilder(RefDTO.fromTmpId(skuTypeTmpId), "获取库存")
                                .tmpId(NncUtils.randomNonNegative())
                                .code("getAmount")
                                .returnTypeRef(getLongType().getRef())
                                .addNode(
                                        NodeDTOFactory.createSelfNode(
                                                NncUtils.randomNonNegative(),
                                                "当前记录",
                                                RefDTO.fromTmpId(skuTypeTmpId)
                                        )
                                )
                                .addNode(
                                        NodeDTOFactory.createReturnNode(
                                                NncUtils.randomNonNegative(),
                                                "返回",
                                                ValueDTOFactory.createRefeference("当前记录.库存")
                                        )
                                )
                                .build()
                )
                .addMethod(
                        MethodDTOBuilder.newBuilder(RefDTO.fromTmpId(skuTypeTmpId), "设置库存")
                                .tmpId(NncUtils.randomNonNegative())
                                .code("setAmount")
                                .returnTypeRef(getVoidType().getRef())
                                .addNode(
                                        NodeDTOFactory.createSelfNode(
                                                NncUtils.randomNonNegative(),
                                                "当前记录",
                                                RefDTO.fromTmpId(skuTypeTmpId)
                                        )
                                )
                                .addNode(
                                        NodeDTOFactory.createInputNode(
                                                NncUtils.randomNonNegative(),
                                                "流程输入",
                                                List.of(
                                                        InputFieldDTO.create("库存", getLongType().getRef())
                                                )
                                        )
                                )
                                .addNode(
                                        NodeDTOFactory.createUpdateObjectNode(
                                                NncUtils.randomNonNegative(),
                                                "更新库存",
                                                ValueDTOFactory.createRefeference("当前记录"),
                                                List.of(
                                                        new UpdateFieldDTO(
                                                                RefDTO.fromTmpId(skuAmountFieldTmpId),
                                                                UpdateOp.SET.code(),
                                                                ValueDTOFactory.createRefeference("流程输入.库存")
                                                        )
                                                )
                                        )
                                )
                                .addNode(
                                        NodeDTOFactory.createReturnNode(
                                                NncUtils.randomNonNegative(),
                                                "返回",
                                                null
                                        )
                                )
                                .build()
                )
                .build()
        );
        var skuTitleFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "title");
        var skuPriceFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "price");
        var skuAmountFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "amount");
        var skuChildArrayTypeId = typeManager.getArrayType(skuTypeDTO.id(), ArrayKind.CHILD.code()).type().id();

        var productTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("商品")
                .code("Product")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldRef(RefDTO.fromTmpId(titleFieldTmpId))
                .addField(FieldDTOBuilder.newBuilder("标题", getStringType().getRef())
                        .code("title")
                        .tmpId(titleFieldTmpId)
                        .build())
                .addField(FieldDTOBuilder.newBuilder("sku列表", RefDTO.fromId(skuChildArrayTypeId))
                        .isChild(true)
                        .code("skuList")
                        .tmpId(NncUtils.randomNonNegative())
                        .build())
                .build()
        );
        var productTitleFieldId = TestUtils.getFieldIdByCode(productTypeDTO, "title");
        var productSkuListFieldId = TestUtils.getFieldIdByCode(productTypeDTO, "skuList");
        var couponStateTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("优惠券状态")
                .code("CouponState")
                .tmpId(NncUtils.randomNonNegative())
                .category(TypeCategory.ENUM.code())
                .build()
        );
        long couponNormalStateId = saveEnumConstant(typeManager, couponStateTypeDTO, "正常", 0);
        long couponUsedStateId = saveEnumConstant(typeManager, couponStateTypeDTO, "已使用", 1);
        var couponTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("优惠券")
                .code("Coupon")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldRef(RefDTO.fromTmpId(titleFieldTmpId))
                .addField(FieldDTOBuilder.newBuilder("标题", getStringType().getRef())
                        .code("title")
                        .tmpId(titleFieldTmpId)
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("折扣", getDoubleType().getRef())
                        .code("discount")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("状态", couponStateTypeDTO.getRef())
                        .code("state")
                        .tmpId(NncUtils.randomNonNegative())
                        .defaultValue(
                                new ReferenceFieldValue(null, PhysicalId.of(couponNormalStateId).toString())
                        )
                        .build()
                )
                .build()
        );
        var couponTitleFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "title");
        var couponDiscountFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "discount");
        var couponStateFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "state");
        long couponArrayTypeId = typeManager.getArrayType(couponTypeDTO.id(), ArrayKind.READ_WRITE.code()).type().id();
        var orderTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("订单")
                .code("Order")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldRef(RefDTO.fromTmpId(titleFieldTmpId))
                .addField(FieldDTOBuilder.newBuilder("编号", getStringType().getRef())
                        .code("code")
                        .tmpId(titleFieldTmpId)
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("商品", productTypeDTO.getRef())
                        .code("product")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("数量", getLongType().getRef())
                        .code("amount")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("价格", getDoubleType().getRef())
                        .code("price")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("优惠券列表", RefDTO.fromId(couponArrayTypeId))
                        .code("coupons")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("时间", getTimeType().getRef())
                        .code("time")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .build()
        );
        var orderCodeFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "code");
        var orderProductFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "product");
        var orderAmountFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "amount");
        var orderPriceFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "price");
        var orderCouponsFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "coupons");
        var orderTimeFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "time");
        return new ShoppingTypeIds(
                productTypeDTO.id(),
                skuTypeDTO.id(),
                couponStateTypeDTO.id(),
                couponTypeDTO.id(),
                orderTypeDTO.id(),
                skuChildArrayTypeId,
                productTitleFieldId,
                productSkuListFieldId,
                skuTitleFieldId,
                skuPriceFieldId,
                skuAmountFieldId,
                couponTitleFieldId,
                couponDiscountFieldId,
                couponStateFieldId,
                orderCodeFieldId,
                orderProductFieldId,
                orderAmountFieldId,
                orderPriceFieldId,
                orderTimeFieldId,
                orderCouponsFieldId,
                couponNormalStateId,
                couponUsedStateId
        );
    }

    private static long saveEnumConstant(TypeManager typeManager, TypeDTO enumType, String name, int ordinal) {
        var enumSuperType =
                typeManager.getType(new GetTypeRequest(enumType.getClassParam().superClassRef().id(), false))
                        .type();
        var enumNameFieldId = TestUtils.getFieldIdByCode(enumSuperType, "name");
        var enumOrdinalField = TestUtils.getFieldIdByCode(enumSuperType, "ordinal");
        TestUtils.beginTransaction();
        var id = typeManager.saveEnumConstant(
                new InstanceDTO(
                        null,
                        RefDTO.fromId(enumType.id()),
                        enumType.name(),
                        name,
                        new ClassInstanceParam(
                                List.of(
                                        InstanceFieldDTO.create(
                                                enumNameFieldId,
                                                PrimitiveFieldValue.createString(name)
                                        ),
                                        InstanceFieldDTO.create(
                                                enumOrdinalField,
                                                PrimitiveFieldValue.createLong(ordinal)
                                        )
                                )
                        )
                )
        );
        TestUtils.commitTransaction();
        return id;
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

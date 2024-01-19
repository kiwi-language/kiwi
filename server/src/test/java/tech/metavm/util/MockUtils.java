package tech.metavm.util;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.MethodDTOBuilder;
import tech.metavm.flow.NodeDTOFactory;
import tech.metavm.flow.UpdateOp;
import tech.metavm.flow.ValueDTOFactory;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
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
        TestUtils.startTransaction();
        var result = typeManager.saveType(typeDTO);
        TestUtils.commitTransaction();;
        return result;
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager) {
        long titleFieldTmpId = NncUtils.randomNonNegative();
        var skuTypeTmpId = NncUtils.randomNonNegative();
        var skuAmountFieldTmpId = NncUtils.randomNonNegative();
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
        TestUtils.startTransaction();
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

    public static FooTypes createFooType(boolean initIds) {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .asTitle().build();
        var barType = ClassTypeBuilder.newBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, getStringType())
                .asTitle().build();
        var barArrayType = new ArrayType(null, barType, ArrayKind.CHILD);
        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barArrayType)
                .isChild(true).build();
        var bazType = ClassTypeBuilder.newBuilder("Baz", "Baz").build();
        var bazBarField = FieldBuilder.newBuilder("bar", "bar", bazType, nullableBarType).build();
        if (initIds)
            TestUtils.initEntityIds(fooType);
        return new FooTypes(fooType, barType, bazType, barArrayType, fooNameField, fooBarsField, barCodeField, bazBarField);
    }

    public static ClassInstance createFoo(FooTypes fooTypes) {
        return ClassInstanceBuilder.newBuilder(fooTypes.fooType())
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
                        )
                ))
                .build();
    }


}

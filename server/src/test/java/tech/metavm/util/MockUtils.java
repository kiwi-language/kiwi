package tech.metavm.util;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.core.*;
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

    public static InstanceDTO createProductDTO(InstanceManager instanceManager, ShoppingTypeIds shoppingTypeIds) {
        var productId = TestUtils.doInTransaction(() -> instanceManager.create(createProductDTO(shoppingTypeIds)));
        return instanceManager.get(productId, 1).instance();
    }

    public static List<InstanceDTO> createCouponDTOs(InstanceManager instanceManager, ShoppingTypeIds shoppingTypeIds) {
        var couponFiveOff = new InstanceDTO(
                null,
                RefDTO.fromId(shoppingTypeIds.couponTypeId()),
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
                                        PrimitiveFieldValue.createLong(5L)
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
                RefDTO.fromId(shoppingTypeIds.couponTypeId()),
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
                                        PrimitiveFieldValue.createLong(10L)
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
                RefDTO.fromId(shoppingTypeIds.couponTypeId()),
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
                                        PrimitiveFieldValue.createLong(15L)
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
                RefDTO.fromId(shoppingTypeIds.productTypeId()),
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
                                                InstanceDTO.createArrayInstance(
                                                        RefDTO.fromId(shoppingTypeIds.skuChildArrayTypeId()),
                                                        true,
                                                        List.of(
                                                                InstanceFieldValue.of(
                                                                        new InstanceDTO(
                                                                                null,
                                                                                RefDTO.fromId(shoppingTypeIds.skuTypeId()),
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
                                                                        RefDTO.fromId(shoppingTypeIds.skuTypeId()),
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
                                                                                RefDTO.fromId(shoppingTypeIds.skuTypeId()),
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

    public static ListTypeIds createListType(TypeManager typeManager, FlowManager flowManager) {
        var nodeTypeIds = createNodeTypes(typeManager, flowManager);
        var listTypeTmpId = NncUtils.randomNonNegative();
        var listValueTypeTmpId = NncUtils.randomNonNegative();
        var ids = batchSaveTypes(typeManager, List.of(
                ClassTypeDTOBuilder.newBuilder("列表")
                        .code("List")
                        .tmpId(listTypeTmpId)
                        .isTemplate(true)
                        .typeParameterRefs(List.of(RefDTO.fromTmpId(listValueTypeTmpId)))
                        .tmpId(listTypeTmpId)
                        .build(),
                new TypeDTO(
                        null,
                        listValueTypeTmpId,
                        "值",
                        "T",
                        TypeCategory.VARIABLE.code(),
                        true,
                        true,
                        new TypeVariableParam(
                                RefDTO.fromTmpId(listTypeTmpId),
                                0,
                                List.of(StandardTypes.getAnyType().getRef())
                        )
                )
        ));
        var listTypeId = ids.get(0);
        var listValueTypeId = ids.get(1);
        var listLabelFieldId = saveField(typeManager, FieldDTOBuilder.newBuilder("标签", getStringType().getRef())
                .tmpId(NncUtils.randomNonNegative())
                .code("label")
                .declaringTypeId(listTypeId)
                .build()
        );
        var nodeTypeDTO = typeManager.getParameterizedType(
                new GetParameterizedTypeRequest(
                        RefDTO.fromId(nodeTypeIds.nodeTypeId()),
                        List.of(RefDTO.fromId(listValueTypeId)),
                        List.of()
                )
        ).type();
        var nodeChildArrayType = typeManager.getArrayType(nodeTypeDTO.id(), ArrayKind.CHILD.code()).type();
        var listNodesFieldId = saveField(typeManager, FieldDTOBuilder.newBuilder("节点列表", nodeChildArrayType.getRef())
                .tmpId(NncUtils.randomNonNegative())
                .isChild(true)
                .code("nodes")
                .declaringTypeId(listTypeId)
                .build()
        );
        var listTypeDTO = typeManager.getType(new GetTypeRequest(listTypeId, false)).type();
        var listViewTypeId = TestUtils.getDefaultViewTypeId(listTypeDTO);
        var nodeViewTypeId = TestUtils.getDefaultViewTypeId(nodeTypeDTO);
        var nodeFromViewMethod = NncUtils.findRequired(
                nodeTypeDTO.getClassParam().flows(),
                m -> "fromView".equals(m.code())
        );
        createFlow(flowManager, MethodDTOBuilder.newBuilder(RefDTO.fromId(listTypeId), "从视图创建")
                .tmpId(NncUtils.randomNonNegative())
                .code("fromView")
                .isStatic(true)
                .returnTypeRef(RefDTO.fromId(listTypeId))
                .parameters(List.of(
                        ParameterDTO.create(NncUtils.randomNonNegative(), "视图", RefDTO.fromId(listViewTypeId))
                ))
                .addNode(
                        NodeDTOFactory.createInputNode(
                                NncUtils.randomNonNegative(),
                                "流程输入",
                                List.of(
                                        InputFieldDTO.create("视图", RefDTO.fromId(listViewTypeId))
                                )
                        )
                )
                .addNode(NodeDTOFactory.createNewArrayNode(
                                NncUtils.randomNonNegative(),
                                "节点列表",
                                RefDTO.fromId(nodeChildArrayType.id())
                        )
                )
                .addNode(NodeDTOFactory.createWhileNode(
                                NncUtils.randomNonNegative(),
                                "循环",
                                ValueDTOFactory.createExpression("循环.索引 < LEN(流程输入.视图.节点列表)"),
                                List.of(
                                        NodeDTOFactory.createGetElementNode(
                                                NncUtils.randomNonNegative(),
                                                "节点视图",
                                                ValueDTOFactory.createReference("流程输入.视图.节点列表"),
                                                ValueDTOFactory.createReference("循环.索引")
                                        ),
                                        NodeDTOFactory.createMethodCallNode(
                                                NncUtils.randomNonNegative(),
                                                "节点",
                                                nodeFromViewMethod.getRef(),
                                                null,
                                                List.of(
                                                        new ArgumentDTO(
                                                                NncUtils.randomNonNegative(),
                                                                nodeFromViewMethod.parameters().get(0).getRef(),
                                                                ValueDTOFactory.createReference("节点视图")
                                                        )
                                                )
                                        ),
                                        NodeDTOFactory.createAddElementNode(
                                                NncUtils.randomNonNegative(),
                                                "添加节点",
                                                ValueDTOFactory.createReference("节点列表"),
                                                ValueDTOFactory.createReference("节点")
                                        )
                                ),
                                List.of(
                                        new LoopFieldDTO(
                                                RefDTO.fromTmpId(NncUtils.randomNonNegative()),
                                                "索引",
                                                getLongType().getRef(),
                                                ValueDTOFactory.createConstant(0L),
                                                ValueDTOFactory.createExpression("循环.索引 + 1")

                                        )
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createAddObjectNode(
                                NncUtils.randomNonNegative(),
                                "列表",
                                RefDTO.fromId(listTypeId),
                                List.of(
                                        FieldParamDTO.create(
                                                RefDTO.fromId(listLabelFieldId),
                                                ValueDTOFactory.createReference("流程输入.视图.标签")
                                        ),
                                        FieldParamDTO.create(
                                                RefDTO.fromId(listNodesFieldId),
                                                ValueDTOFactory.createReference("节点列表")
                                        )
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                "返回",
                                ValueDTOFactory.createReference("列表")
                        )
                )
                .build()
        );
        return new ListTypeIds(
                listTypeId,
                listValueTypeId,
                listLabelFieldId,
                listNodesFieldId,
                nodeTypeIds
        );
    }

    public static NodeTypeIds createNodeTypes(TypeManager typeManager, FlowManager flowManager) {
        var nodeTypeTmpId = NncUtils.randomNonNegative();
        var valueTypeTmpId = NncUtils.randomNonNegative();
        var ids = batchSaveTypes(typeManager,
                List.of(
                        ClassTypeDTOBuilder.newBuilder("节点")
                                .code("Node")
                                .tmpId(nodeTypeTmpId)
                                .isTemplate(true)
                                .typeParameterRefs(List.of(RefDTO.fromTmpId(valueTypeTmpId)))
                                .build(),
                        new TypeDTO(
                                null,
                                valueTypeTmpId,
                                "值",
                                "T",
                                TypeCategory.VARIABLE.code(),
                                true,
                                true,
                                new TypeVariableParam(
                                        RefDTO.fromTmpId(nodeTypeTmpId),
                                        0,
                                        List.of(StandardTypes.getAnyType().getRef())
                                )
                        )
                )
        );
        var nodeTypeId = ids.get(0);
        var valueTypeId = ids.get(1);
        var nodeLabelFieldId = TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("标签", getStringType().getRef())
                        .tmpId(NncUtils.randomNonNegative())
                        .code("label")
                        .declaringTypeId(nodeTypeId)
                        .build()
        ));
        var nodeValueFieldId = TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("值", RefDTO.fromId(valueTypeId))
                        .tmpId(NncUtils.randomNonNegative())
                        .code("value")
                        .declaringTypeId(nodeTypeId)
                        .build()
        ));
        var nodeTypeDTO = typeManager.getType(new GetTypeRequest(nodeTypeId, false)).type();
        var defaultMapping = NncUtils.findRequired(
                nodeTypeDTO.getClassParam().mappings(),
                m -> m.getRef().equals(nodeTypeDTO.getClassParam().defaultMappingRef())
        );
        var viewTypeRef = defaultMapping.targetTypeRef();
        createFlow(flowManager, MethodDTOBuilder.newBuilder(RefDTO.fromId(nodeTypeId), "从视图创建")
                .tmpId(NncUtils.randomNonNegative())
                .code("fromView")
                .isStatic(true)
                .returnTypeRef(RefDTO.fromId(nodeTypeId))
                .parameters(List.of(
                        ParameterDTO.create(NncUtils.randomNonNegative(), "视图", viewTypeRef)
                ))
                .addNode(
                        NodeDTOFactory.createInputNode(
                                NncUtils.randomNonNegative(),
                                "流程输入",
                                List.of(
                                        InputFieldDTO.create("视图", viewTypeRef)
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createAddObjectNode(
                                NncUtils.randomNonNegative(),
                                "节点",
                                RefDTO.fromId(nodeTypeId),
                                List.of(
                                        FieldParamDTO.create(
                                                RefDTO.fromId(nodeLabelFieldId),
                                                ValueDTOFactory.createReference("流程输入.视图.标签")
                                        ),
                                        FieldParamDTO.create(
                                                RefDTO.fromId(nodeValueFieldId),
                                                ValueDTOFactory.createReference("流程输入.视图.值")
                                        )
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                "返回",
                                ValueDTOFactory.createReference("节点")
                        )
                )
                .build());
        return new NodeTypeIds(
                nodeTypeId,
                valueTypeId,
                nodeLabelFieldId,
                nodeValueFieldId
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

    private static long saveField(TypeManager typeManager, FieldDTO fieldDTO) {
        return TestUtils.doInTransaction(() -> typeManager.saveField(fieldDTO));
    }

    private static TypeDTO saveType(TypeManager typeManager, TypeDTO typeDTO) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.saveType(typeDTO));
    }

    private static List<Long> batchSaveTypes(TypeManager typeManager, List<TypeDTO> typeDTOs) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(typeDTOs, List.of(), List.of())
        ));
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager, FlowManager flowManager) {
        long titleFieldTmpId = NncUtils.randomNonNegative();
        var skuTypeTmpId = NncUtils.randomNonNegative();
        var skuAmountFieldTmpId = NncUtils.randomNonNegative();
        var skuDecAmountMethodTmpId = NncUtils.randomNonNegative();

        var couponStateTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("优惠券状态")
                .code("CouponState")
                .tmpId(NncUtils.randomNonNegative())
                .category(TypeCategory.ENUM.code())
                .build()
        );
        var couponNormalStateId = saveEnumConstant(typeManager, couponStateTypeDTO, "正常", 0);
        var couponUsedStateId = saveEnumConstant(typeManager, couponStateTypeDTO, "已使用", 1);
        couponStateTypeDTO = typeManager.getType(new GetTypeRequest(couponStateTypeDTO.id(), false)).type();
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
//                        .defaultValue(new ReferenceFieldValue(null, PhysicalId.of(couponNormalStateId).toString()))
                                        .build()
                        )
                        .build()
        );
        var couponTitleFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "title");
        var couponDiscountFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "discount");
        var couponStateFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "state");
        long couponArrayTypeId = typeManager.getArrayType(couponTypeDTO.id(), ArrayKind.READ_WRITE.code()).type().id();
        var couponUseMethodId = createFlow(flowManager, createCouponUseMethod(couponTypeDTO, couponStateTypeDTO));

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
                                                ValueDTOFactory.createReference("当前记录.库存")
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
                                                ValueDTOFactory.createReference("当前记录"),
                                                List.of(
                                                        new UpdateFieldDTO(
                                                                RefDTO.fromTmpId(skuAmountFieldTmpId),
                                                                UpdateOp.SET.code(),
                                                                ValueDTOFactory.createReference("流程输入.库存")
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
                .addMethod(
                        MethodDTOBuilder.newBuilder(RefDTO.fromTmpId(skuTypeTmpId), "扣减库存")
                                .code("decAmount")
                                .tmpId(skuDecAmountMethodTmpId)
                                .parameters(List.of(
                                        ParameterDTO.create(NncUtils.randomNonNegative(), "数量", getLongType().getRef())
                                ))
                                .returnTypeRef(getVoidType().getRef())
                                .addNode(NodeDTOFactory.createSelfNode(
                                        NncUtils.randomNonNegative(),
                                        "当前记录",
                                        RefDTO.fromTmpId(skuTypeTmpId)
                                ))
                                .addNode(NodeDTOFactory.createInputNode(
                                        NncUtils.randomNonNegative(),
                                        "流程输入",
                                        List.of(
                                                InputFieldDTO.create("数量", getLongType().getRef())
                                        )
                                ))
                                .addNode(NodeDTOFactory.createBranchNode(
                                        NncUtils.randomNonNegative(),
                                        "判断库存是否足够",
                                        List.of(
                                                NodeDTOFactory.createBranch(
                                                        NncUtils.randomNonNegative(),
                                                        1,
                                                        ValueDTOFactory.createExpression("流程输入.数量 <= 当前记录.库存"),
                                                        false,
                                                        List.of(
                                                                NodeDTOFactory.createUpdateObjectNode(
                                                                        NncUtils.randomNonNegative(),
                                                                        "更新库存",
                                                                        ValueDTOFactory.createReference("当前记录"),
                                                                        List.of(
                                                                                new UpdateFieldDTO(
                                                                                        RefDTO.fromTmpId(skuAmountFieldTmpId),
                                                                                        UpdateOp.DEC.code(),
                                                                                        ValueDTOFactory.createReference("流程输入.数量")
                                                                                )
                                                                        )
                                                                ),
                                                                NodeDTOFactory.createReturnNode(
                                                                        NncUtils.randomNonNegative(),
                                                                        "返回",
                                                                        null
                                                                )
                                                        )
                                                ),
                                                NodeDTOFactory.createBranch(
                                                        NncUtils.randomNonNegative(),
                                                        100,
                                                        ValueDTOFactory.createConstant(true),
                                                        true,
                                                        List.of(
                                                                NodeDTOFactory.createRaiseNode(
                                                                        NncUtils.randomNonNegative(),
                                                                        "抛出异常",
                                                                        ValueDTOFactory.createConstant("库存不足")
                                                                )
                                                        )
                                                )
                                        )
                                ))
                                .build()
                )
                .build()
        );

        var skuTitleFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "title");
        var skuPriceFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "price");
        var skuAmountFieldId = TestUtils.getFieldIdByCode(skuTypeDTO, "amount");
        var skuDecAmountMethodId = TestUtils.getMethodIdByCode(skuTypeDTO, "decAmount");
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
        var orderTypeDTO = saveType(typeManager, ClassTypeDTOBuilder.newBuilder("订单")
                .code("Order")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldRef(RefDTO.fromTmpId(titleFieldTmpId))
                .addField(FieldDTOBuilder.newBuilder("编号", getStringType().getRef())
                        .code("code")
                        .tmpId(titleFieldTmpId)
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("sku", skuTypeDTO.getRef())
                        .code("sku")
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
        var orderSkuFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "sku");
        var orderAmountFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "amount");
        var orderPriceFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "price");
        var orderCouponsFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "coupons");
        var orderTimeFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "time");
        createFlow(flowManager, createSkuFromViewMethod(skuTypeDTO));

        var skuBuyMethodId = createFlow(
                flowManager,
                createBuyMethod(
                        skuTypeDTO,
                        couponArrayTypeId,
                        orderTypeDTO,
                        couponUseMethodId
                )
        );

        return new ShoppingTypeIds(
                productTypeDTO.id(),
                skuTypeDTO.id(),
                couponStateTypeDTO.id(),
                couponTypeDTO.id(),
                orderTypeDTO.id(),
                skuChildArrayTypeId,
                couponArrayTypeId,
                productTitleFieldId,
                productSkuListFieldId,
                skuTitleFieldId,
                skuPriceFieldId,
                skuAmountFieldId,
                skuDecAmountMethodId,
                skuBuyMethodId,
                couponTitleFieldId,
                couponDiscountFieldId,
                couponStateFieldId,
                orderCodeFieldId,
                orderSkuFieldId,
                orderAmountFieldId,
                orderPriceFieldId,
                orderTimeFieldId,
                orderCouponsFieldId,
                couponNormalStateId,
                couponUsedStateId
        );
    }

    private static long createFlow(FlowManager flowManager, FlowDTO flow) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction((() -> flowManager.save(flow))).getId();
    }

    private static FlowDTO createBuyMethod(TypeDTO skuTypeDTO, long couponArrayTypeId, TypeDTO orderTypeDTO, long couponUseMethodId) {
        var amountParameterTmpId = NncUtils.randomNonNegative();
        var orderCodeFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "code");
        var orderSkuFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "sku");
        var orderAmountFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "amount");
        var orderPriceFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "price");
        var orderCouponsFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "coupons");
        var orderTimeFieldId = TestUtils.getFieldIdByCode(orderTypeDTO, "time");

        return MethodDTOBuilder.newBuilder(RefDTO.fromId(skuTypeDTO.id()), "购买")
                .tmpId(NncUtils.randomNonNegative())
                .code("buy")
                .returnTypeRef(RefDTO.fromId(orderTypeDTO.id()))
                .parameters(List.of(
                        ParameterDTO.create(amountParameterTmpId, "数量", getLongType().getRef()),
                        ParameterDTO.create(NncUtils.randomNonNegative(), "优惠券列表", RefDTO.fromId(couponArrayTypeId))
                ))
                .addNode(NodeDTOFactory.createSelfNode(
                        NncUtils.randomNonNegative(),
                        "当前记录",
                        RefDTO.fromId(skuTypeDTO.id())
                ))
                .addNode(NodeDTOFactory.createInputNode(
                                NncUtils.randomNonNegative(),
                                "流程输入",
                                List.of(
                                        InputFieldDTO.create("数量", getLongType().getRef()),
                                        InputFieldDTO.create("优惠券列表", RefDTO.fromId(couponArrayTypeId))
                                )
                        )
                )
                .addNode(NodeDTOFactory.createMethodCallNode(
                                NncUtils.randomNonNegative(),
                                "调用扣减库存方法",
                                RefDTO.fromId(TestUtils.getMethodIdByCode(skuTypeDTO, "decAmount")),
                                ValueDTOFactory.createReference("当前记录"),
                                List.of(
                                        new ArgumentDTO(
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromTmpId(amountParameterTmpId),
                                                ValueDTOFactory.createReference("流程输入.数量")
                                        )
                                )
                        )
                )
                .addNode(NodeDTOFactory.createWhileNode(
                                NncUtils.randomNonNegative(),
                                "遍历优惠券列表",
                                ValueDTOFactory.createExpression("遍历优惠券列表.索引 < len(流程输入.优惠券列表)"),
                                List.of(
                                        NodeDTOFactory.createValueNode(
                                                NncUtils.randomNonNegative(),
                                                "优惠券",
                                                ValueDTOFactory.createExpression("流程输入.优惠券列表[遍历优惠券列表.索引]")
                                        ),
                                        NodeDTOFactory.createMethodCallNode(
                                                NncUtils.randomNonNegative(),
                                                "调用优惠券使用方法",
                                                RefDTO.fromId(couponUseMethodId),
                                                ValueDTOFactory.createReference("优惠券"),
                                                List.of()
                                        )
                                ),
                                List.of(
                                        new LoopFieldDTO(
                                                RefDTO.fromTmpId(NncUtils.randomNonNegative()),
                                                "索引",
                                                getLongType().getRef(),
                                                ValueDTOFactory.createConstant(0L),
                                                ValueDTOFactory.createReference("遍历优惠券列表.索引 + 1")
                                        ),
                                        new LoopFieldDTO(
                                                RefDTO.fromTmpId(NncUtils.randomNonNegative()),
                                                "总折扣",
                                                getDoubleType().getRef(),
                                                ValueDTOFactory.createConstant(0.0),
                                                ValueDTOFactory.createReference("遍历优惠券列表.总折扣 + 优惠券.折扣")
                                        )
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createAddObjectNode(
                                NncUtils.randomNonNegative(),
                                "创建订单",
                                RefDTO.fromId(orderTypeDTO.id()),
                                List.of(
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderCodeFieldId),
                                                ValueDTOFactory.createReference("当前记录.标题")
                                        ),
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderSkuFieldId),
                                                ValueDTOFactory.createReference("当前记录")
                                        ),
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderAmountFieldId),
                                                ValueDTOFactory.createReference("流程输入.数量")
                                        ),
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderPriceFieldId),
                                                ValueDTOFactory.createExpression("当前记录.价格 * 流程输入.数量 - 遍历优惠券列表.总折扣")
                                        ),
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderCouponsFieldId),
                                                ValueDTOFactory.createReference("流程输入.优惠券列表")
                                        ),
                                        new FieldParamDTO(
                                                null,
                                                NncUtils.randomNonNegative(),
                                                RefDTO.fromId(orderTimeFieldId),
                                                ValueDTOFactory.createExpression("now()")
                                        )
                                )
                        )
                )
                .addNode(
                        NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                "返回",
                                ValueDTOFactory.createReference("创建订单")
                        )
                )
                .build();
    }

    private static FlowDTO createSkuFromViewMethod(TypeDTO skuTypeDTO) {
        var defaultMapping = NncUtils.findRequired(
                skuTypeDTO.getClassParam().mappings(),
                m -> m.getRef().equals(skuTypeDTO.getClassParam().defaultMappingRef())
        );
        var viewTypeRef = defaultMapping.targetTypeRef();
        return MethodDTOBuilder.newBuilder(skuTypeDTO.getRef(), "从视图创建")
                .tmpId(NncUtils.randomNonNegative())
                .isStatic(true)
                .code("fromView")
                .returnTypeRef(skuTypeDTO.getRef())
                .parameters(List.of(
                        ParameterDTO.create(NncUtils.randomNonNegative(), "视图", viewTypeRef)
                ))
                .addNode(NodeDTOFactory.createInputNode(
                        NncUtils.randomNonNegative(),
                        "流程输入",
                        List.of(
                                InputFieldDTO.create("视图", viewTypeRef)
                        )
                ))
                .addNode(NodeDTOFactory.createAddObjectNode(
                        NncUtils.randomNonNegative(),
                        "对象",
                        RefDTO.fromId(skuTypeDTO.id()),
                        List.of(
                                new FieldParamDTO(
                                        null,
                                        NncUtils.randomNonNegative(),
                                        RefDTO.fromId(TestUtils.getFieldIdByCode(skuTypeDTO, "title")),
                                        ValueDTOFactory.createExpression("流程输入.视图.标题")
                                ),
                                new FieldParamDTO(
                                        null,
                                        NncUtils.randomNonNegative(),
                                        RefDTO.fromId(TestUtils.getFieldIdByCode(skuTypeDTO, "price")),
                                        ValueDTOFactory.createExpression("流程输入.视图.价格")
                                ),
                                new FieldParamDTO(
                                        null,
                                        NncUtils.randomNonNegative(),
                                        RefDTO.fromId(TestUtils.getFieldIdByCode(skuTypeDTO, "amount")),
                                        ValueDTOFactory.createExpression("流程输入.视图.库存")
                                )
                        )
                ))
                .addNode(NodeDTOFactory.createReturnNode(
                        NncUtils.randomNonNegative(),
                        "返回",
                        ValueDTOFactory.createReference("对象")
                ))
                .build();
    }

    private static FlowDTO createCouponUseMethod(TypeDTO couponTypeDTO, TypeDTO couponStateTypeDTO) {
        var couponStateFieldId = TestUtils.getFieldIdByCode(couponTypeDTO, "state");
        var couponStateUsedId = TestUtils.getEnumConstantIdByName(couponStateTypeDTO, "已使用");
        return MethodDTOBuilder.newBuilder(RefDTO.fromId(couponTypeDTO.id()), "使用")
                .tmpId(NncUtils.randomNonNegative())
                .returnTypeRef(getVoidType().getRef())
                .code("use")
                .returnTypeRef(getVoidType().getRef())
                .addNode(NodeDTOFactory.createSelfNode(
                        NncUtils.randomNonNegative(),
                        "当前记录",
                        RefDTO.fromId(couponTypeDTO.id())
                ))
                .addNode(NodeDTOFactory.createBranchNode(
                        NncUtils.randomNonNegative(),
                        "判断是否已使用",
                        List.of(
                                NodeDTOFactory.createBranch(
                                        NncUtils.randomNonNegative(),
                                        1,
                                        ValueDTOFactory.createExpression("当前记录.状态 = $$" + couponStateUsedId),
                                        false,
                                        List.of(
                                                NodeDTOFactory.createRaiseNode(
                                                        NncUtils.randomNonNegative(),
                                                        "抛出异常",
                                                        ValueDTOFactory.createConstant("优惠券已使用")
                                                )
                                        )
                                ),
                                NodeDTOFactory.createBranch(
                                        NncUtils.randomNonNegative(),
                                        100,
                                        ValueDTOFactory.createConstant(true),
                                        true,
                                        List.of()
                                )
                        )
                ))
                .addNode(NodeDTOFactory.createUpdateObjectNode(
                        NncUtils.randomNonNegative(),
                        "更新状态",
                        ValueDTOFactory.createReference("当前记录"),
                        List.of(
                                new UpdateFieldDTO(
                                        RefDTO.fromId(couponStateFieldId),
                                        UpdateOp.SET.code(),
                                        ValueDTOFactory.createConstant(Id.parse(couponStateUsedId))
                                )
                        )
                ))
                .addNode(NodeDTOFactory.createReturnNode(
                        NncUtils.randomNonNegative(),
                        "返回",
                        null
                ))
                .build();
    }

    private static String saveEnumConstant(TypeManager typeManager, TypeDTO enumType, String name, int ordinal) {
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
                        null,
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
        return PhysicalId.of(id).toString();
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

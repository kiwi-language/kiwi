package org.metavm.util;

import org.metavm.asm.AssemblerFactory;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FlowSavingContext;
import org.metavm.flow.MethodDTOBuilder;
import org.metavm.flow.NodeDTOFactory;
import org.metavm.flow.ValueDTOFactory;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Baz;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.object.type.rest.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUtils {

    public static ShoppingTypes createShoppingTypes() {
        var productType = KlassBuilder.newBuilder("Product", "Product").build();
        var skuType = KlassBuilder.newBuilder("SKU", "SKU").build();
        var couponType = KlassBuilder.newBuilder("Coupon", "Coupon").build();
        var couponArrayType = new ArrayType(couponType.getType(), ArrayKind.READ_WRITE);
        var orderType = KlassBuilder.newBuilder("Order", "Order").build();
        var couponStateType = KlassBuilder.newBuilder("CouponState", "CouponState")
                .kind(ClassKind.ENUM)
                .build();
        var enumKlass = StdKlass.enum_.get();
        var subst = new SubstitutorV2(
                enumKlass, enumKlass.getTypeParameters(), List.of(couponStateType.getType()),
                ResolutionStage.DEFINITION
        );
        var couponStateEnumKlas = (Klass) subst.visitKlass(enumKlass);
        couponStateType.setSuperType(couponStateEnumKlas.getType());
        var enumNameField = couponStateEnumKlas.getFieldByCode("name");
        var enumOrdinalField = couponStateEnumKlas.getFieldByCode("ordinal");
        var couponNormalState = ClassInstanceBuilder.newBuilder(couponStateType.getType())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("NORMAL"),
                        enumOrdinalField,
                        Instances.longInstance(0L)
                ))
                .id(TmpId.of(NncUtils.randomNonNegative()))
                .build();
        var couponUsedState = ClassInstanceBuilder.newBuilder(couponStateType.getType())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("USED"),
                        enumOrdinalField,
                        Instances.longInstance(1L)
                ))
                .id(TmpId.of(NncUtils.randomNonNegative()))
                .build();
        createEnumConstantField(couponNormalState);
        createEnumConstantField(couponUsedState);
        var productTitleField = FieldBuilder.newBuilder("title", "title", productType, Types.getStringType())
                .asTitle()
                .build();
        var skuChildArrayType = new ArrayType(skuType.getType(), ArrayKind.CHILD);
        var productSkuListField = FieldBuilder.newBuilder("skuList", "skuList", productType, skuChildArrayType)
                .isChild(true)
                .build();
        var skuTitleField = FieldBuilder.newBuilder("title", "title", skuType, Types.getStringType())
                .asTitle()
                .build();
        var skuPriceField = FieldBuilder.newBuilder("price", "price", skuType, Types.getDoubleType())
                .build();
        var skuAmountField = FieldBuilder.newBuilder("amount", "amount", skuType, Types.getLongType())
                .access(Access.PRIVATE)
                .build();
        var orderCodeField = FieldBuilder.newBuilder("code", "code", orderType, Types.getStringType())
                .asTitle()
                .build();
        var orderProductField = FieldBuilder.newBuilder("product", "product", orderType, productType.getType()).build();
        var orderCouponsField = FieldBuilder.newBuilder("coupons", "coupons", orderType, couponArrayType)
                .isChild(true).build();
        var orderAmountField = FieldBuilder.newBuilder("amount", "amount", orderType, Types.getLongType()).build();
        var orderPriceField = FieldBuilder.newBuilder("price", "price", orderType, Types.getDoubleType()).build();
        var orderTimeField = FieldBuilder.newBuilder("time", "time", orderType, Types.getTimeType()).build();
        var couponTitleField = FieldBuilder.newBuilder("title", "title", couponType, Types.getStringType())
                .asTitle()
                .build();
        var couponDiscountField = FieldBuilder.newBuilder("discount", "discount", couponType, Types.getDoubleType())
                .build();
        var couponStateField = FieldBuilder.newBuilder("state", "state", couponType, couponStateType.getType())
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
                TypeExpressions.getClassType(shoppingTypeIds.couponTypeId()),
                "coupon",
                "5 Yuan Off",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("5 Yuan Off")
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
                TypeExpressions.getClassType(shoppingTypeIds.couponTypeId()),
                "coupon",
                "10 Yuan Off",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("10 Yuan Off")
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
                TypeExpressions.getClassType(shoppingTypeIds.couponTypeId()),
                "coupon",
                "15 Yuan Off",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.couponTitleFieldId(),
                                        PrimitiveFieldValue.createString("15 Yuan Off")
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
                TypeExpressions.getClassType(shoppingTypeIds.productTypeId()),
                "Product",
                "Shoes",
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.productTitleFieldId(),
                                        PrimitiveFieldValue.createString("Shoes")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.productSkuListFieldId(),
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        shoppingTypeIds.skuChildListType(),
                                                        true,
                                                        List.of(
                                                                InstanceFieldValue.of(
                                                                        new InstanceDTO(
                                                                                null,
                                                                                TypeExpressions.getClassType(shoppingTypeIds.skuTypeId()),
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
                                                                        TypeExpressions.getClassType(shoppingTypeIds.skuTypeId()),
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
                                                                                TypeExpressions.getClassType(shoppingTypeIds.skuTypeId()),
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
        var sku40 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("40"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku41 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("41"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku42 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("42"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var product = ClassInstanceBuilder.newBuilder(shoppingTypes.productType().getType())
                .data(Map.of(
                        shoppingTypes.productTitleField(),
                        Instances.stringInstance("shoes"),
                        shoppingTypes.productSkuListField(),
                        new ArrayInstance(shoppingTypes.skuChildArrayType(),
                                List.of(sku40, sku41, sku42))
                ))
                .build();
        var couponFiveOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("5 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.longInstance(5L)
                ))
                .build();
        var couponTenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("10 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.longInstance(10L)
                ))
                .build();
        var couponFifteenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("15 Yuan Off"),
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

    public static ListTypeIds createListType(TypeManager typeManager, EntityContextFactory entityContextFactory) {
        saveListTypes(typeManager, entityContextFactory);
        var listType = typeManager.getTypeByCode("MyList").type();
        return new ListTypeIds(
                listType.id(),
                listType.typeParameterIds().get(0),
                TestUtils.getFieldIdByCode(listType, "label"),
                TestUtils.getFieldIdByCode(listType, "nodes"),
                getNodeTypeIds(typeManager)
        );
    }

    private static void saveListTypes(TypeManager typeManager, EntityContextFactory entityContextFactory) {
        assemble(PROJECT_ROOT + "/test/src/test/resources/asm/List.masm", typeManager, entityContextFactory);
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
        var enumType = enumConstant.getKlass();
        var nameField = enumType.getFieldByCode("name");
        var name = enumConstant.getStringField(nameField).getValue();
        return FieldBuilder.newBuilder(name, null, enumType, enumType.getType())
                .isStatic(true)
                .staticValue(enumConstant)
                .build();
    }

    private static KlassDTO saveType(TypeManager typeManager, KlassDTO klassDTO) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.saveType(klassDTO));
    }

    private static List<String> batchSaveTypes(TypeManager typeManager, List<KlassDTO> klassDTOS) {
        FlowSavingContext.initConfig();
        return TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(klassDTOS, List.of(), false)
        ));
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager, EntityContextFactory entityContextFactory) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm", typeManager, entityContextFactory);
        var productType = typeManager.getTypeByCode("Product").type();
        var skuType = typeManager.getTypeByCode("SKU").type();
        var couponType = typeManager.getTypeByCode("Coupon").type();
        var couponStateType = typeManager.getTypeByCode("CouponState").type();
        var orderType = typeManager.getTypeByCode("Order").type();
        var skuChildListType = TypeExpressions.getChildListType(TypeExpressions.getClassType(skuType.id()));
        var couponListType = TypeExpressions.getReadWriteListType(TypeExpressions.getClassType(couponType.id()));
        return new ShoppingTypeIds(
                productType.id(),
                skuType.id(),
                couponStateType.id(),
                couponType.id(),
                orderType.id(),
                skuChildListType,
                couponListType,
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

    public static LivingBeingTypeIds createLivingBeingTypes(TypeManager typeManager, EntityContextFactory entityContextFactory) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm", typeManager, entityContextFactory);
        var livingBeingType = typeManager.getTypeByCode("LivingBeing").type();
        var animalType = typeManager.getTypeByCode("Animal").type();
        var humanType = typeManager.getTypeByCode("Human").type();
        var sentientType = typeManager.getTypeByCode("Sentient").type();
        return new LivingBeingTypeIds(
                livingBeingType.id(),
                animalType.id(),
                humanType.id(),
                sentientType.id(),
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

    public static void assemble(String source, TypeManager typeManager, EntityContextFactory entityContextFactory) {
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var assembler = AssemblerFactory.createWithStandardTypes(context);
            assembler.assemble(List.of(source));
            FlowSavingContext.initConfig();
            TestUtils.doInTransaction(() -> typeManager.batchSave(new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true)));
        }
    }

    public static FooTypes createFooTypes() {
        return createFooTypes(false);
    }

    public static FooTypes createFooTypes(boolean initIds) {
        var fooType = KlassBuilder.newBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, Types.getStringType())
                .asTitle().build();
        var fooCodeField = FieldBuilder.newBuilder("code", "code", fooType, Types.getNullableStringType())
                .build();
        var barType = KlassBuilder.newBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, Types.getStringType())
                .asTitle().build();
        var barChildArrayType = new ArrayType(barType.getType(), ArrayKind.CHILD);
        var barArrayType = new ArrayType(barType.getType(), ArrayKind.READ_WRITE);
//        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barChildArrayType)
                .isChild(true).build();
        var bazType = KlassBuilder.newBuilder("Baz", "Baz").build();
        var bazArrayType = new ArrayType(bazType.getType(), ArrayKind.READ_WRITE);
        var bazBarsField = FieldBuilder.newBuilder("bars", "bars", bazType, barArrayType).build();
        var fooBazListField = FieldBuilder.newBuilder("bazList", "bazList", fooType, bazArrayType).build();
        var quxType = KlassBuilder.newBuilder("Qux", "Qux").build();
        var quxAmountField = FieldBuilder.newBuilder("amount", "amount", quxType, Types.getLongType()).build();
        var nullableQuxType = new UnionType(Set.of(quxType.getType(), Types.getNullType()));
        var fooQuxField = FieldBuilder.newBuilder("qux", "qux", fooType, nullableQuxType).build();
        if (initIds)
            TestUtils.initEntityIds(fooType);
        return new FooTypes(fooType, barType, quxType, bazType, barArrayType, barChildArrayType, bazArrayType, fooNameField,
                fooCodeField, fooBarsField, fooQuxField, fooBazListField, barCodeField, bazBarsField, quxAmountField);
    }

    public static LivingBeingTypes createLivingBeingTypes(boolean initIds) {
        var livingBeingType = KlassBuilder.newBuilder("LivingBeing", "LivingBeing").build();
        var livingBeingAgeField = FieldBuilder.newBuilder("age", "age", livingBeingType, Types.getLongType())
                .build();
        var livingBeingExtraInfoField = FieldBuilder.newBuilder("extraInfo", "extraInfo", livingBeingType, Types.getAnyType())
                .build();
        var livingBeingArrayType = new ArrayType(livingBeingType.getType(), ArrayKind.READ_WRITE);
        var livingBeingOffspringsField = FieldBuilder.newBuilder("offsprings", "offsprings", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var livingBeingAncestorsField = FieldBuilder.newBuilder("ancestors", "ancestors", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var animalType = KlassBuilder.newBuilder("Animal", "Animal")
                .superClass(livingBeingType.getType())
                .build();
        var animalIntelligenceField = FieldBuilder.newBuilder("intelligence", "intelligence", animalType, Types.getLongType())
                .build();
        var humanType = KlassBuilder.newBuilder("Human", "Human")
                .superClass(animalType.getType())
                .build();
        var humanOccupationField = FieldBuilder.newBuilder("occupation", "occupation", humanType, Types.getStringType())
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
        var human = ClassInstanceBuilder.newBuilder(livingBeingTypes.humanType().getType())
                .data(Map.of(
                        livingBeingTypes.livingBeingAgeField(),
                        Instances.longInstance(30L),
                        livingBeingTypes.livingBeingAncestorsField(),
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()),
                        livingBeingTypes.livingBeingOffspringsField(),
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()),
                        livingBeingTypes.livingBeingExtraInfoFIeld(),
                        Instances.stringInstance("very smart"),
                        livingBeingTypes.animalIntelligenceField(),
                        Instances.longInstance(160L),
                        livingBeingTypes.humanOccupationField(),
                        Instances.stringInstance("programmer")
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
        var foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType().getType())
                .data(Map.of(
                        fooTypes.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(
                                fooTypes.barChildArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar001")
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar002")
                                                ))
                                                .build()
                                )
                        ),
                        fooTypes.fooQuxField(),
                        ClassInstanceBuilder.newBuilder(fooTypes.quxType().getType())
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
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar003")
                                                                                ))
                                                                                .build(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar004")
                                                                                ))
                                                                                .build()
                                                                )
                                                        )
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar005")
                                                                                ))
                                                                                .build(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
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
        var platformUserTypeDTO = ClassTypeDTOBuilder.newBuilder("PlatformUser")
                .code("PlatformUser")
                .tmpId(NncUtils.randomNonNegative())
                .titleFieldId(TmpId.of(NncUtils.randomNonNegative()).toString())
                .addField(FieldDTOBuilder.newBuilder("loginName", "string")
                        .code("loginName")
                        .asTitle(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("password", "password")
                        .code("password")
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .build();
        var applicationTypeTmpId = TmpId.random().toString();
        var applicationTypeDTO = ClassTypeDTOBuilder.newBuilder("Application")
                .id(applicationTypeTmpId)
                .code("Application")
                .titleFieldId(TmpId.random().toString())
                .addField(FieldDTOBuilder.newBuilder("name", "string")
                        .code("name")
                        .asTitle(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addField(FieldDTOBuilder.newBuilder("owner", TypeExpressions.getClassType(platformUserTypeDTO.id()))
                        .code("owner")
                        .access(Access.PRIVATE.code())
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
                )
                .addMethod(MethodDTOBuilder.newBuilder(applicationTypeTmpId, "getOwner")
                        .tmpId(NncUtils.randomNonNegative())
                        .code("getOwner")
                        .returnType(TypeExpressions.getClassType(platformUserTypeDTO.id()))
                        .addNode(NodeDTOFactory.createSelfNode(
                                NncUtils.randomNonNegative(),
                                "self",
                                applicationTypeTmpId
                        ))
                        .addNode(NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                "return",
                                ValueDTOFactory.createReference("self.owner")
                        ))
                        .build())
                .build();
        var typeIds = TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(List.of(platformUserTypeDTO, applicationTypeDTO), List.of(), false)
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

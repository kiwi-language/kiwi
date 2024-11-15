package org.metavm.util;

import org.metavm.asm.AssemblerFactory;
import org.metavm.ddl.CommitState;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FlowSavingContext;
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
import org.metavm.object.type.rest.dto.BatchSaveRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUtils {

    public static ShoppingTypes createShoppingTypes() {
        var productType = TestUtils.newKlassBuilder("Product", "Product").build();
        var skuType = TestUtils.newKlassBuilder("SKU", "SKU").build();
        var couponType = TestUtils.newKlassBuilder("Coupon", "Coupon").build();
        var couponArrayType = new ArrayType(couponType.getType(), ArrayKind.READ_WRITE);
        var orderType = TestUtils.newKlassBuilder("Order", "Order").build();
        var couponStateType = TestUtils.newKlassBuilder("CouponState", "CouponState")
                .kind(ClassKind.ENUM)
                .build();
        var enumKlass = StdKlass.enum_.get();
        var couponStateEnumKlas = enumKlass.getParameterized(List.of(couponStateType.getType()));
        couponStateType.setSuperType(couponStateEnumKlas.getType());
        var enumNameField = couponStateEnumKlas.getFieldByName("name");
        var enumOrdinalField = couponStateEnumKlas.getFieldByName("ordinal");
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
        var productTitleField = FieldBuilder.newBuilder("title", productType, Types.getStringType())
                .asTitle()
                .build();
        var skuChildArrayType = new ArrayType(skuType.getType(), ArrayKind.CHILD);
        var productSkuListField = FieldBuilder.newBuilder("skuList", productType, skuChildArrayType)
                .isChild(true)
                .build();
        var skuTitleField = FieldBuilder.newBuilder("title", skuType, Types.getStringType())
                .asTitle()
                .build();
        var skuPriceField = FieldBuilder.newBuilder("price", skuType, Types.getDoubleType())
                .build();
        var skuAmountField = FieldBuilder.newBuilder("amount", skuType, Types.getLongType())
                .access(Access.PRIVATE)
                .build();
        var orderCodeField = FieldBuilder.newBuilder("code", orderType, Types.getStringType())
                .asTitle()
                .build();
        var orderProductField = FieldBuilder.newBuilder("product", orderType, productType.getType()).build();
        var orderCouponsField = FieldBuilder.newBuilder("coupons", orderType, couponArrayType)
                .isChild(true).build();
        var orderAmountField = FieldBuilder.newBuilder("amount", orderType, Types.getLongType()).build();
        var orderPriceField = FieldBuilder.newBuilder("price", orderType, Types.getDoubleType()).build();
        var orderTimeField = FieldBuilder.newBuilder("time", orderType, Types.getTimeType()).build();
        var couponTitleField = FieldBuilder.newBuilder("title", couponType, Types.getStringType())
                .asTitle()
                .build();
        var couponDiscountField = FieldBuilder.newBuilder("discount", couponType, Types.getDoubleType())
                .build();
        var couponStateField = FieldBuilder.newBuilder("state", couponType, couponStateType.getType())
                .defaultValue(couponNormalState.getReference())
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
                                List.of(sku40.getReference(), sku41.getReference(), sku42.getReference())).getReference()
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

    public static ListTypeIds createListType(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        saveListTypes(typeManager, schedulerAndWorker);
        var listType = typeManager.getTypeByQualifiedName("MyList").type();
        return new ListTypeIds(
                listType.id(),
                listType.typeParameterIds().get(0),
                TestUtils.getFieldIdByName(listType, "label"),
                TestUtils.getFieldIdByName(listType, "nodes"),
                getNodeTypeIds(typeManager)
        );
    }

    private static void saveListTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble(PROJECT_ROOT + "/test/src/test/resources/asm/List.masm", typeManager, schedulerAndWorker);
    }

    private static NodeTypeIds getNodeTypeIds(TypeManager typeManager) {
        var nodeType = typeManager.getTypeByQualifiedName("Node").type();
        return new NodeTypeIds(
                nodeType.id(),
                nodeType.typeParameterIds().get(0),
                TestUtils.getFieldIdByName(nodeType, "label"),
                TestUtils.getFieldIdByName(nodeType, "value")
        );
    }

    private static Field createEnumConstantField(ClassInstance enumConstant) {
        var enumType = enumConstant.getKlass();
        var nameField = enumType.getFieldByName("name");
        var name = enumConstant.getStringField(nameField).getValue();
        return FieldBuilder.newBuilder(name, enumType, enumType.getType())
                .isStatic(true)
                .staticValue(enumConstant.getReference())
                .build();
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm", typeManager, schedulerAndWorker);
        var productType = typeManager.getTypeByQualifiedName("Product").type();
        var skuType = typeManager.getTypeByQualifiedName("SKU").type();
        var couponType = typeManager.getTypeByQualifiedName("Coupon").type();
        var couponStateType = typeManager.getTypeByQualifiedName("CouponState").type();
        var orderType = typeManager.getTypeByQualifiedName("Order").type();
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
                TestUtils.getFieldIdByName(productType, "name"),
                TestUtils.getFieldIdByName(productType, "skuList"),
                TestUtils.getFieldIdByName(skuType, "name"),
                TestUtils.getFieldIdByName(skuType, "price"),
                TestUtils.getFieldIdByName(skuType, "quantity"),
                TestUtils.getMethodIdByCode(skuType, "decQuantity"),
                TestUtils.getMethodIdByCode(skuType, "buy"),
                TestUtils.getFieldIdByName(couponType, "name"),
                TestUtils.getFieldIdByName(couponType, "discount"),
                TestUtils.getFieldIdByName(couponType, "state"),
                TestUtils.getFieldIdByName(orderType, "code"),
                TestUtils.getFieldIdByName(orderType, "sku"),
                TestUtils.getFieldIdByName(orderType, "quantity"),
                TestUtils.getFieldIdByName(orderType, "price"),
                TestUtils.getFieldIdByName(orderType, "orderTime"),
                TestUtils.getFieldIdByName(orderType, "coupons"),
                typeManager.getEnumConstant(couponStateType.id(), "NORMAL").id(),
                typeManager.getEnumConstant(couponStateType.id(), "USED").id()
        );
    }

    public static LivingBeingTypeIds createLivingBeingTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm", typeManager, schedulerAndWorker);
        var livingBeingType = typeManager.getTypeByQualifiedName("LivingBeing").type();
        var animalType = typeManager.getTypeByQualifiedName("Animal").type();
        var humanType = typeManager.getTypeByQualifiedName("Human").type();
        var sentientType = typeManager.getTypeByQualifiedName("Sentient").type();
        return new LivingBeingTypeIds(
                livingBeingType.id(),
                animalType.id(),
                humanType.id(),
                sentientType.id(),
                TestUtils.getFieldIdByName(livingBeingType, "age"),
                TestUtils.getFieldIdByName(livingBeingType, "extra"),
                TestUtils.getFieldIdByName(livingBeingType, "offsprings"),
                TestUtils.getFieldIdByName(livingBeingType, "ancestors"),
                TestUtils.getFieldIdByName(animalType, "intelligence"),
                TestUtils.getFieldIdByName(humanType, "occupation"),
                TestUtils.getFieldIdByName(humanType, "thinking"),
                TestUtils.getMethodIdByCode(livingBeingType, "LivingBeing"),
                TestUtils.getMethodIdByCode(animalType, "Animal"),
                TestUtils.getMethodIdByCode(humanType, "Human"),
                TestUtils.getMethodIdByCode(livingBeingType, "makeSound"),
                TestUtils.getMethodIdByCode(sentientType, "think")
        );
    }

    public static void assemble(String source, TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble(source, typeManager, true, schedulerAndWorker);
    }

    public static String assemble(String source, TypeManager typeManager, boolean waitForDDLDone, SchedulerAndWorker schedulerAndWorker) {
        ContextUtil.setAppId(TestConstants.APP_ID);
        var entityContextFactory = schedulerAndWorker.entityContextFactory();
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var assembler = AssemblerFactory.createWithStandardTypes(context);
            assembler.assemble(List.of(source));
            FlowSavingContext.initConfig();
            var request = new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true);
            NncUtils.writeFile("/Users/leen/workspace/object/test.json", NncUtils.toPrettyJsonString(request));
            var commitId = TestUtils.doInTransaction(() -> typeManager.batchSave(request));
            if (waitForDDLDone)
                TestUtils.waitForDDLState(CommitState.COMPLETED, schedulerAndWorker);
            return commitId;
        }
    }

    public static FooTypes createFooTypes() {
        return createFooTypes(false);
    }

    public static FooTypes createFooTypes(boolean initIds) {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", fooType, Types.getStringType())
                .asTitle().build();
        var fooCodeField = FieldBuilder.newBuilder("code", fooType, Types.getNullableStringType())
                .build();
        var barType = TestUtils.newKlassBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", barType, Types.getStringType())
                .asTitle().build();
        var barChildArrayType = new ArrayType(barType.getType(), ArrayKind.CHILD);
        var barArrayType = new ArrayType(barType.getType(), ArrayKind.READ_WRITE);
//        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", fooType, barChildArrayType)
                .isChild(true).build();
        var bazType = TestUtils.newKlassBuilder("Baz", "Baz").build();
        var bazArrayType = new ArrayType(bazType.getType(), ArrayKind.READ_WRITE);
        var bazBarsField = FieldBuilder.newBuilder("bars", bazType, barArrayType).build();
        var fooBazListField = FieldBuilder.newBuilder("bazList", fooType, bazArrayType).build();
        var quxType = TestUtils.newKlassBuilder("Qux", "Qux").build();
        var quxAmountField = FieldBuilder.newBuilder("amount", quxType, Types.getLongType()).build();
        var nullableQuxType = new UnionType(Set.of(quxType.getType(), Types.getNullType()));
        var fooQuxField = FieldBuilder.newBuilder("qux", fooType, nullableQuxType).build();
        if (initIds)
            TestUtils.initEntityIds(fooType);
        return new FooTypes(fooType, barType, quxType, bazType, barArrayType, barChildArrayType, bazArrayType, fooNameField,
                fooCodeField, fooBarsField, fooQuxField, fooBazListField, barCodeField, bazBarsField, quxAmountField);
    }

    public static LivingBeingTypes createLivingBeingTypes(boolean initIds) {
        var livingBeingType = TestUtils.newKlassBuilder("LivingBeing", "LivingBeing").build();
        var livingBeingAgeField = FieldBuilder.newBuilder("age", livingBeingType, Types.getLongType())
                .build();
        var livingBeingExtraInfoField = FieldBuilder.newBuilder("extraInfo", livingBeingType, Types.getAnyType())
                .build();
        var livingBeingArrayType = new ArrayType(livingBeingType.getType(), ArrayKind.READ_WRITE);
        var livingBeingOffspringsField = FieldBuilder.newBuilder("offsprings", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var livingBeingAncestorsField = FieldBuilder.newBuilder("ancestors", livingBeingType, livingBeingArrayType)
                .isChild(true)
                .build();
        var animalType = TestUtils.newKlassBuilder("Animal", "Animal")
                .superType(livingBeingType.getType())
                .build();
        var animalIntelligenceField = FieldBuilder.newBuilder("intelligence", animalType, Types.getLongType())
                .build();
        var humanType = TestUtils.newKlassBuilder("Human", "Human")
                .superType(animalType.getType())
                .build();
        var humanOccupationField = FieldBuilder.newBuilder("occupation", humanType, Types.getStringType())
                .build();
        if (initIds)
            TestUtils.initEntityIds(humanType);
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
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()).getReference(),
                        livingBeingTypes.livingBeingOffspringsField(),
                        new ArrayInstance(livingBeingTypes.livingBeingArrayType()).getReference(),
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
                                                .buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar002")
                                                ))
                                                .buildAndGetReference()
                                )
                        ).getReference(),
                        fooTypes.fooQuxField(),
                        ClassInstanceBuilder.newBuilder(fooTypes.quxType().getType())
                                .data(
                                        Map.of(
                                                fooTypes.quxAmountField(),
                                                Instances.longInstance(100L)
                                        )
                                )
                                .buildAndGetReference(),
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
                                                                                .buildAndGetReference(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar004")
                                                                                ))
                                                                                .buildAndGetReference()
                                                                )
                                                        ).getReference()
                                                ))
                                                .buildAndGetReference(),
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
                                                                                .buildAndGetReference(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar006")
                                                                                ))
                                                                                .buildAndGetReference()
                                                                )
                                                        ).getReference()
                                                ))
                                                .buildAndGetReference()
                                )
                        ).getReference()
                ))
                .build();
        if (initIds)
            TestUtils.initInstanceIds(foo);
        return foo;
    }

    public static UserTypeIds createUserTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/User.masm", typeManager, schedulerAndWorker);
        var applicationType = typeManager.getTypeByQualifiedName("Application").type();
        var applicationNameFieldId = TestUtils.getFieldIdByName(applicationType, "name");
        var applicationOwnerFieldId = TestUtils.getFieldIdByName(applicationType, "owner");
        // get the longName field id and password field id of the platform user type
        var platformUserType = typeManager.getTypeByQualifiedName("PlatformUser").type();
        var platformUserLoginNameFieldId = TestUtils.getFieldIdByName(platformUserType, "loginName");
        var platformUserPasswordFieldId = TestUtils.getFieldIdByName(platformUserType, "passwd");
        return new UserTypeIds(platformUserType.id(), applicationType.id(), applicationNameFieldId, applicationOwnerFieldId, platformUserLoginNameFieldId,
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

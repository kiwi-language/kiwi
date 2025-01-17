package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.asm.AssemblerFactory;
import org.metavm.ddl.CommitState;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FlowSavingContext;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Baz;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
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
        var couponStateEnumKlas = KlassType.create(enumKlass, List.of(couponStateType.getType()));
        couponStateType.setSuperType(couponStateEnumKlas);
        var enumNameField = couponStateEnumKlas.getKlass().getFieldByName("name");
        var enumOrdinalField = couponStateEnumKlas.getKlass().getFieldByName("ordinal");
        var couponNormalState = ClassInstanceBuilder.newBuilder(couponStateType.getType())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("NORMAL"),
                        enumOrdinalField,
                        Instances.intInstance(0)
                ))
                .id(TmpId.of(Utils.randomNonNegative()))
                .build();
        var couponUsedState = ClassInstanceBuilder.newBuilder(couponStateType.getType())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("USED"),
                        enumOrdinalField,
                        Instances.intInstance(1)
                ))
                .id(TmpId.of(Utils.randomNonNegative()))
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
                        Instances.doubleInstance(5)
                ))
                .build();
        var couponTenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("10 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.doubleInstance(10L)
                ))
                .build();
        var couponFifteenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("15 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.doubleInstance(15)
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
        var enumType = enumConstant.getInstanceKlass();
        var nameField = enumType.getFieldByName("name");
        var name = enumConstant.getStringField(nameField).getValue();
        return FieldBuilder.newBuilder(name, enumType, enumType.getType())
                .isStatic(true)
                .staticValue(enumConstant.getReference())
                .build();
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm", typeManager, schedulerAndWorker);
        var entityContextFactory = schedulerAndWorker.entityContextFactory();
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var productKlass = context.getKlassByQualifiedName("Product");
            var skuKlass = context.getKlassByQualifiedName("SKU");
            var couponKlass = context.getKlassByQualifiedName("Coupon");
            var couponStateKlas = context.getKlassByQualifiedName("CouponState");
            var orderKlass = context.getKlassByQualifiedName("Order");
            var skuChildListType = TypeExpressions.getChildListType(TypeExpressions.getClassType(skuKlass.getStringId()));
            var couponListType = TypeExpressions.getReadWriteListType(TypeExpressions.getClassType(couponKlass.getStringId()));
            return new ShoppingTypeIds(
                    productKlass.getStringId(),
                    skuKlass.getStringId(),
                    couponStateKlas.getStringId(),
                    couponKlass.getStringId(),
                    orderKlass.getStringId(),
                    skuChildListType,
                    couponListType,
                    productKlass.getFieldByName("name").getStringId(),
                    productKlass.getFieldByName("skuList").getStringId(),
                    skuKlass.getFieldByName("name").getStringId(),
                    skuKlass.getFieldByName("price").getStringId(),
                    skuKlass.getFieldByName("quantity").getStringId(),
                    skuKlass.getMethodByName("decQuantity").getStringId(),
                    skuKlass.getMethodByName("buy").getStringId(),
                    couponKlass.getFieldByName("name").getStringId(),
                    couponKlass.getFieldByName("discount").getStringId(),
                    couponKlass.getFieldByName("state").getStringId(),
                    orderKlass.getFieldByName("code").getStringId(),
                    orderKlass.getFieldByName("sku").getStringId(),
                    orderKlass.getFieldByName("quantity").getStringId(),
                    orderKlass.getFieldByName("price").getStringId(),
                    orderKlass.getFieldByName("orderTime").getStringId(),
                    orderKlass.getFieldByName("coupons").getStringId(),
                    typeManager.getEnumConstantId("CouponState", "NORMAL"),
                    typeManager.getEnumConstantId("CouponState", "USED")
            );
        }
    }

    public static LivingBeingTypeIds createLivingBeingTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm", typeManager, schedulerAndWorker);
        var entityContextFactory = schedulerAndWorker.entityContextFactory();
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var livingBeingKlass = context.getKlassByQualifiedName("LivingBeing");
            var animalKlas = context.getKlassByQualifiedName("Animal");
            var humanKlass = context.getKlassByQualifiedName("Human");
            var sentientKlass = context.getKlassByQualifiedName("Sentient");
            return new LivingBeingTypeIds(
                    livingBeingKlass.getStringId(),
                    animalKlas.getStringId(),
                    humanKlass.getStringId(),
                    sentientKlass.getStringId(),
                    livingBeingKlass.getFieldByName("age").getStringId(),
                    livingBeingKlass.getFieldByName("extra").getStringId(),
                    livingBeingKlass.getFieldByName("offsprings").getStringId(),
                    livingBeingKlass.getFieldByName("ancestors").getStringId(),
                    animalKlas.getFieldByName("intelligence").getStringId(),
                    humanKlass.getFieldByName("occupation").getStringId(),
                    humanKlass.getFieldByName("thinking").getStringId(),
                    livingBeingKlass.getMethodByName("LivingBeing").getStringId(),
                    animalKlas.getMethodByName("Animal").getStringId(),
                    humanKlass.getMethodByName("Human").getStringId(),
                    livingBeingKlass.getMethodByName("makeSound").getStringId(),
                    sentientKlass.getMethodByName("think").getStringId()
            );
        }
    }

    public static void assemble(String source, TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble(source, typeManager, true, schedulerAndWorker);
    }

    public static String assemble(String source, TypeManager typeManager, boolean waitForDDLDone, SchedulerAndWorker schedulerAndWorker) {
        ContextUtil.setAppId(TestConstants.APP_ID);
        var entityContextFactory = schedulerAndWorker.entityContextFactory();
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            context.loadKlasses();
            var assembler = AssemblerFactory.createWithStandardTypes(context);
            assembler.assemble(List.of(source));
            assembler.generateClasses(TestConstants.TARGET);
            FlowSavingContext.initConfig();
//            var request = new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true);
//            NncUtils.writeFile("/Users/leen/workspace/object/test.json", NncUtils.toPrettyJsonString(request));
            var commitId = TestUtils.doInTransaction(() -> {
                try(var input = new FileInputStream(TestConstants.TARGET + "/target.mva")) {
                    return typeManager.deploy(input);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            if (waitForDDLDone)
                TestUtils.waitForDDLState(CommitState.COMPLETED, schedulerAndWorker);
            return commitId;
        }
    }

    public static FooTypes createFooTypes() {
        return createFooTypes(false);
    }

    public static FooTypes createFooTypes(boolean initIds) {
        var fooKlass = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType())
                .asTitle().build();
        var fooCodeField = FieldBuilder.newBuilder("code", fooKlass, Types.getNullableStringType())
                .build();
        var barKlass = TestUtils.newKlassBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", barKlass, Types.getStringType())
                .asTitle().build();
        var barChildArrayType = new ArrayType(barKlass.getType(), ArrayKind.CHILD);
        var barArrayType = new ArrayType(barKlass.getType(), ArrayKind.READ_WRITE);
//        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", fooKlass, barChildArrayType)
                .isChild(true).build();
        var bazKlass = TestUtils.newKlassBuilder("Baz", "Baz").build();
        var bazArrayType = new ArrayType(bazKlass.getType(), ArrayKind.READ_WRITE);
        var bazBarsField = FieldBuilder.newBuilder("bars", bazKlass, barArrayType).build();
        var fooBazListField = FieldBuilder.newBuilder("bazList", fooKlass, bazArrayType).build();
        var quxKlass = TestUtils.newKlassBuilder("Qux", "Qux").build();
        var quxAmountField = FieldBuilder.newBuilder("amount", quxKlass, Types.getLongType()).build();
        var nullableQuxType = new UnionType(Set.of(quxKlass.getType(), Types.getNullType()));
        var fooQuxField = FieldBuilder.newBuilder("qux", fooKlass, nullableQuxType).build();
        if (initIds) {
            TestUtils.initEntityIds(fooKlass);
            TestUtils.initEntityIds(bazKlass);
            TestUtils.initEntityIds(quxKlass);
            TestUtils.initEntityIds(bazKlass);
        }
//        log.debug("{}", fooKlass.getText());
//        log.debug("{}", barKlass.getText());
//        log.debug("{}", quxKlass.getText());
//        log.debug("{}", bazKlass.getText());
        return new FooTypes(fooKlass, barKlass, quxKlass, bazKlass, barArrayType, barChildArrayType, bazArrayType, fooNameField,
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

    public static Foo getFoo() {
        Foo foo = new Foo("Big Foo", null);
        foo.setBar(new Bar(foo, "Bar001"));
        foo.setCode("Foo001");

        foo.setQux(new Qux(100));
        Baz baz1 = new Baz();
        baz1.setBars(List.of(new Bar(null, "Bar002")));
        Baz baz2 = new Baz();
        foo.setBazList(List.of(baz1, baz2));
        return foo;
    }
}

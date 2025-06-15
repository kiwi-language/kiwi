package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.CompilationTask;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.MockEnter;
import org.metavm.ddl.CommitState;
import org.metavm.entity.StdKlass;
import org.metavm.flow.*;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Baz;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class MockUtils {

    public static ShoppingTypes createShoppingTypes() {
        var productType = TestUtils.newKlassBuilder("Product", "Product").build();
        var skuType = TestUtils.newKlassBuilder("SKU", "SKU").build();
        var couponType = TestUtils.newKlassBuilder("Coupon", "Coupon").build();
        var couponArrayType = new ArrayType(couponType.getType(), ArrayKind.DEFAULT);
        var orderType = TestUtils.newKlassBuilder("Order", "Order").build();
        var couponStateType = TestUtils.newKlassBuilder("CouponState", "CouponState")
                .kind(ClassKind.ENUM)
                .build();
        var enumKlass = StdKlass.enum_.get();
        var couponStateEnumKlas = KlassType.create(enumKlass, List.of(couponStateType.getType()));
        couponStateType.setSuperType(couponStateEnumKlas);
        var enumNameField = couponStateEnumKlas.getKlass().getFieldByName("name");
        var enumOrdinalField = couponStateEnumKlas.getKlass().getFieldByName("ordinal");
        var couponNormalState = ClassInstanceBuilder.newBuilder(couponStateType.getType(), TmpId.random())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("NORMAL"),
                        enumOrdinalField,
                        Instances.intInstance(0)
                ))
                .build();
        var couponUsedState = ClassInstanceBuilder.newBuilder(couponStateType.getType(), TmpId.random())
                .data(Map.of(
                        enumNameField,
                        Instances.stringInstance("USED"),
                        enumOrdinalField,
                        Instances.intInstance(1)
                ))
                .build();
        createEnumConstantField(couponNormalState);
        createEnumConstantField(couponUsedState);
        var productTitleField = FieldBuilder.newBuilder("title", productType, Types.getStringType())
                .asTitle()
                .build();
        var skuArrayType = new ArrayType(skuType.getType(), ArrayKind.DEFAULT);
        var productSkuListField = FieldBuilder.newBuilder("skuList", productType, skuArrayType).build();
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
        var orderCouponsField = FieldBuilder.newBuilder("coupons", orderType, couponArrayType).build();
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
                productType, skuType, couponType, orderType, couponStateType, skuArrayType, couponArrayType,
                productTitleField, productSkuListField, skuTitleField, skuPriceField, skuAmountField,
                couponTitleField, couponDiscountField, couponStateField, orderCodeField, orderProductField,
                orderCouponsField, orderAmountField, orderPriceField, orderTimeField, couponNormalState, couponUsedState
        );
    }

    public static ShoppingInstances createShoppingInstances(ShoppingTypes shoppingTypes) {
        var sku40 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("40"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku41 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("41"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var sku42 = ClassInstanceBuilder.newBuilder(shoppingTypes.skuType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.skuTitleField(),
                        Instances.stringInstance("42"),
                        shoppingTypes.skuAmountField(),
                        Instances.longInstance(100),
                        shoppingTypes.skuPriceField(),
                        Instances.doubleInstance(100)
                ))
                .build();
        var product = ClassInstanceBuilder.newBuilder(shoppingTypes.productType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.productTitleField(),
                        Instances.stringInstance("shoes"),
                        shoppingTypes.productSkuListField(),
                        new ArrayInstance(shoppingTypes.skuChildArrayType(),
                                List.of(sku40.getReference(), sku41.getReference(), sku42.getReference())).getReference()
                ))
                .build();
        var couponFiveOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("5 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.doubleInstance(5)
                ))
                .build();
        var couponTenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType(), TmpId.random())
                .data(Map.of(
                        shoppingTypes.couponTitleField(),
                        Instances.stringInstance("10 Yuan Off"),
                        shoppingTypes.couponDiscountField(),
                        Instances.doubleInstance(10L)
                ))
                .build();
        var couponFifteenOff = ClassInstanceBuilder.newBuilder(shoppingTypes.couponType().getType(), TmpId.random())
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
        var name = enumConstant.getStringField(nameField);
        return FieldBuilder.newBuilder(name, enumType, enumType.getType())
                .isStatic(true)
                .staticValue(enumConstant.getReference())
                .build();
    }

    public static ShoppingTypeIds createShoppingTypes(TypeManager typeManager, SchedulerAndWorker schedulerAndWorker) {
        assemble("kiwi/Shopping.kiwi", typeManager, schedulerAndWorker);
        var entityContextFactory = schedulerAndWorker.entityContextFactory();
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var productKlass = context.getKlassByQualifiedName("Product");
            var skuKlass = context.getKlassByQualifiedName("SKU");
            var couponKlass = context.getKlassByQualifiedName("Coupon");
            var couponStateKlas = context.getKlassByQualifiedName("CouponState");
            var orderKlass = context.getKlassByQualifiedName("Order");
            var skuListType = TypeExpressions.getArrayListType(TypeExpressions.getClassType(skuKlass.getStringId()));
            var couponListType = TypeExpressions.getArrayListType(TypeExpressions.getClassType(couponKlass.getStringId()));
            return new ShoppingTypeIds(
                    productKlass.getStringId(),
                    skuKlass.getStringId(),
                    couponStateKlas.getStringId(),
                    couponKlass.getStringId(),
                    orderKlass.getStringId(),
                    skuListType,
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
        assemble("kiwi/LivingBeing.kiwi", typeManager, schedulerAndWorker);
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

            var task = new CompilationTask(List.of(Path.of(TestUtils.getResourcePath(source))), TestConstants.TARGET);
            task.parse();
            MockEnter.enterStandard(task.getProject());
            task.analyze();
            if (task.getErrorCount() > 0)
                throw new CompilationException("Compilation failed");
            task.generate();

            FlowSavingContext.initConfig();
//            var request = new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true);
            var commitId = TestUtils.doInTransaction(() -> {
                try(var input = new FileInputStream(TestConstants.TARGET + "/target.mva")) {
                    return typeManager.deploy(input);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            if (waitForDDLDone)
                TestUtils.waitForDDLCompleted(schedulerAndWorker);
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
        var bardArrayType = new ArrayType(barKlass.getType(), ArrayKind.DEFAULT);
        var barArrayType = new ArrayType(barKlass.getType(), ArrayKind.DEFAULT);
//        var nullableBarType = new UnionType(null, Set.of(barType, getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", fooKlass, bardArrayType).build();
        var bazKlass = TestUtils.newKlassBuilder("Baz", "Baz").build();
        var bazArrayType = new ArrayType(bazKlass.getType(), ArrayKind.DEFAULT);
        var bazBarsField = FieldBuilder.newBuilder("bars", bazKlass, barArrayType).build();
        var fooBazListField = FieldBuilder.newBuilder("bazList", fooKlass, bazArrayType).build();
        var quxKlass = TestUtils.newKlassBuilder("Qux", "Qux").build();
        var quxAmountField = FieldBuilder.newBuilder("amount", quxKlass, Types.getLongType()).build();
        var nullableQuxType = new UnionType(Set.of(quxKlass.getType(), Types.getNullType()));
        var fooQuxField = FieldBuilder.newBuilder("qux", fooKlass, nullableQuxType).build();
//        log.debug("{}", fooKlass.getText());
//        log.debug("{}", barKlass.getText());
//        log.debug("{}", quxKlass.getText());
//        log.debug("{}", bazKlass.getText());
        return new FooTypes(fooKlass, barKlass, quxKlass, bazKlass, barArrayType, bardArrayType, bazArrayType, fooNameField,
                fooCodeField, fooBarsField, fooQuxField, fooBazListField, barCodeField, bazBarsField, quxAmountField);
    }

    public static LivingBeingTypes createLivingBeingTypes(boolean initIds) {
        var livingBeingType = TestUtils.newKlassBuilder("LivingBeing", "LivingBeing").build();
        var livingBeingAgeField = FieldBuilder.newBuilder("age", livingBeingType, Types.getLongType())
                .build();
        var livingBeingExtraInfoField = FieldBuilder.newBuilder("extraInfo", livingBeingType, Types.getAnyType())
                .build();
        var livingBeingArrayType = new ArrayType(livingBeingType.getType(), ArrayKind.DEFAULT);
        var livingBeingOffspringsField = FieldBuilder.newBuilder("offsprings", livingBeingType, livingBeingArrayType)
                .build();
        var livingBeingAncestorsField = FieldBuilder.newBuilder("ancestors", livingBeingType, livingBeingArrayType)
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

    public static ClassInstance createHuman(LivingBeingTypes livingBeingTypes, Id id) {
        var human = ClassInstanceBuilder.newBuilder(livingBeingTypes.humanType().getType(), id)
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
        return human;
    }

    public static ClassInstance createFoo(FooTypes fooTypes) {
        return createFoo(fooTypes, TmpId::random);
    }

    public static ClassInstance createFoo(FooTypes fooTypes, Supplier<Id> idSupplier) {
        var foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType().getType(), idSupplier.get())
                .data(Map.of(
                        fooTypes.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(
                                fooTypes.barChildArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar001")
                                                ))
                                                .buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
                                                .data(Map.of(
                                                        fooTypes.barCodeField(),
                                                        Instances.stringInstance("bar002")
                                                ))
                                                .buildAndGetReference()
                                )
                        ).getReference(),
                        fooTypes.fooQuxField(),
                        ClassInstanceBuilder.newBuilder(fooTypes.quxType().getType(), idSupplier.get())
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
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType(), idSupplier.get())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar003")
                                                                                ))
                                                                                .buildAndGetReference(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar004")
                                                                                ))
                                                                                .buildAndGetReference()
                                                                )
                                                        ).getReference()
                                                ))
                                                .buildAndGetReference(),
                                        ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType(), idSupplier.get())
                                                .data(Map.of(
                                                        fooTypes.bazBarsField(),
                                                        new ArrayInstance(
                                                                fooTypes.barArrayType(),
                                                                List.of(
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
                                                                                .data(Map.of(
                                                                                        fooTypes.barCodeField(),
                                                                                        Instances.stringInstance("bar005")
                                                                                ))
                                                                                .buildAndGetReference(),
                                                                        ClassInstanceBuilder.newBuilder(fooTypes.barType().getType(), idSupplier.get())
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
        return foo;
    }

    public static Function<Supplier<Id>, Foo> getFooCreator() {
        return allocator -> {
            Foo foo = new Foo(allocator.get(), "Big Foo", null);
            foo.setBar(new Bar(foo.nextChildId(), foo, "Bar001"));
            foo.setCode("Foo001");

            foo.setQux(new Qux(allocator.get(), 100));
            Baz baz1 = new Baz(allocator.get());
            baz1.setBars(List.of(new Bar(allocator.get(), null, "Bar002")));
            Baz baz2 = new Baz(allocator.get());
            foo.setBazList(List.of(baz1, baz2));
            return foo;
        };
    }

    public static List<Klass> createTestKlasses() {
        var supplierKlass = TestUtils.newKlassBuilder("Supplier").kind(ClassKind.INTERFACE).build();
        var supplierTypeParam = new TypeVariable(supplierKlass.nextChildId(), "T", supplierKlass);
        MethodBuilder.newBuilder(supplierKlass, "get")
                .returnType(supplierTypeParam.getType())
                .isAbstract(true)
                .build();
        var fooKlass = TestUtils.newKlassBuilder("Foo").searchable(true).build();
        var typeParam = new TypeVariable(fooKlass.nextChildId(), "T", fooKlass);
        fooKlass.setInterfaces(List.of(KlassType.create(supplierKlass, List.of(typeParam.getType()))));
        var nameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType()).build();
        var valueField = FieldBuilder.newBuilder("value", fooKlass, typeParam.getType()).build();

        var constructor = MethodBuilder.newBuilder(fooKlass, "Foo")
                .isConstructor(true)
                .parameters(
                        new NameAndType("name", Types.getStringType()),
                        new NameAndType("value", typeParam.getType())
                )
                .build();
        {
            var code = constructor.getCode();
            Nodes.this_(code);
            Nodes.argument(constructor, 0);
            Nodes.setField(nameField.getRef(), code);
            Nodes.this_(code);
            Nodes.argument(constructor, 1);
            Nodes.setField(valueField.getRef(), code);
            Nodes.this_(code);
            Nodes.ret(code);
        }
        var getNameMethod = MethodBuilder.newBuilder(fooKlass, "getName")
                .returnType(Types.getStringType())
                .build();
        {
            var code = getNameMethod.getCode();
            Nodes.this_(code);
            Nodes.getField(nameField.getRef(), code);
            Nodes.ret(code);
        }
        var getMethod = MethodBuilder.newBuilder(fooKlass, "get")
                .returnType(typeParam.getType())
                .build();
        {
            var code = getMethod.getCode();
            Nodes.this_(code);
            Nodes.getField(valueField.getRef(), code);
            Nodes.ret(code);
        }
        var factoryMethod = MethodBuilder.newBuilder(fooKlass, "create").isStatic(true).build();
        var factoryTypeParam = new TypeVariable(fooKlass.nextChildId(), "T", factoryMethod);
        var pKlass = KlassType.create(fooKlass, List.of(factoryTypeParam.getType()));
        factoryMethod.setParameters(List.of(
                new Parameter(fooKlass.nextChildId(), "name", Types.getStringType(), factoryMethod),
                new Parameter(fooKlass.nextChildId(), "value", factoryTypeParam.getType(), factoryMethod)
        ));
        factoryMethod.setReturnType(pKlass);
        {
            var code = factoryMethod.getCode();
            Nodes.argument(factoryMethod, 0);
            Nodes.argument(factoryMethod, 1);
            Nodes.newObject(code, pKlass, false, false);
            Nodes.dup(code);
            Nodes.invokeMethod(pKlass.getMethod(MethodRef::isConstructor), code);
            Nodes.ret(code);
        }
        var longFooKlass = KlassType.create(fooKlass, List.of(Types.getLongType()));
        var getComparatorMethod = MethodBuilder.newBuilder(fooKlass, "getComparator")
                .isStatic(true)
                .returnType(new FunctionType(List.of(longFooKlass, longFooKlass), Types.getLongType()))
                .build();
        var lambda = new Lambda(fooKlass.nextChildId(), TmpId.randomString(), List.of(), Types.getLongType(), getComparatorMethod);
        lambda.setParameters(List.of(
                new Parameter(fooKlass.nextChildId(), "foo1", longFooKlass, lambda),
                new Parameter(fooKlass.nextChildId(), "foo2", longFooKlass, lambda)
        ));
        {
            var code = lambda.getCode();
            Nodes.argument(lambda, 0);
            Nodes.argument(lambda, 1);
            Nodes.compareEq(Types.getLongType(), code);
            var if1 = Nodes.ifNe(null, code);
            Nodes.argument(lambda, 0);
            Nodes.argument(lambda, 1);
            Nodes.lt(code);
            var if2 = Nodes.ifNe(null, code);
            Nodes.loadConstant(Instances.longOne(), code);
            Nodes.ret(code);
            if2.setTarget(Nodes.label(code));
            Nodes.loadConstant(Instances.longInstance(-1), code);
            Nodes.ret(code);
            if1.setTarget(Nodes.label(code));
            Nodes.loadConstant(Instances.longZero(), code);
            Nodes.ret(code);
        }
        {
            var code = getComparatorMethod.getCode();
            Nodes.lambda(lambda, code);
            Nodes.ret(code);
        }
        var indexType = KlassType.create(StdKlass.index.get(), List.of(Types.getStringType(), fooKlass.getType()));
        var nameIdxInitializer = MethodBuilder.newBuilder(fooKlass, "__nameIdx__")
                .isStatic(true)
                .returnType(indexType)
                .build();
        {
            var lambda1 = new Lambda(fooKlass.nextChildId(),
                    "nameIdxLambda1",
                    List.of(),
                    Types.getStringType(),
                    nameIdxInitializer);
            lambda1.addParameter(new Parameter(fooKlass.nextChildId(), "foo", fooKlass.getType(), lambda1));
            {
                var lambdaCode = lambda1.getCode();
                Nodes.load(0, fooKlass.getType(), lambdaCode);
                Nodes.invokeVirtual(getNameMethod.getRef(), lambdaCode);
                Nodes.ret(lambdaCode);
            }
            var code = nameIdxInitializer.getCode();
            Nodes.newObject(code, indexType, false, false);
            Nodes.dup(code);
            Nodes.loadConstant(Instances.stringInstance("nameIdx"), code);
            Nodes.loadConstant(Instances.intInstance(true), code);
            Nodes.lambda(lambda1, code);
            var indexConstructor = StdKlass.index.get().getMethod(m ->
                    m.isConstructor() && m.getParameters().size() == 3 &&
                            m.getParameters().getLast().getType() instanceof FunctionType
                    );
            Nodes.invokeSpecial(indexConstructor.getRef(), code);
            Nodes.ret(code);
        }
        var nameIdxField = FieldBuilder.newBuilder("nameIdx", fooKlass, indexType)
                .isStatic(true)
                .initializer(nameIdxInitializer)
                .build();
        var getByNameMethod = MethodBuilder.newBuilder(fooKlass, "getByName")
                .isStatic(true)
                .parameters(new NameAndType("name", Types.getStringType()))
                .returnType(fooKlass.getType())
                .build();
        {
            var code = getByNameMethod.getCode();
            Nodes.argument(getByNameMethod, 0);
            Nodes.getStaticField(nameIdxField, code);
            var getFirstMethod = StdKlass.index.get().getMethod(m -> m.getName().equals("getFirst"));
            Nodes.invokeVirtual(getFirstMethod.getRef(), code);
            Nodes.nonNull(code);
            Nodes.ret(code);
        }
        fooKlass.emitCode();
        return List.of(fooKlass, supplierKlass);

    }
}

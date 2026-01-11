package org.manul.compiler;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.manul.common.ErrorCode;
import org.manul.compiler.util.CompilationException;
import org.manul.entity.Attribute;
import org.manul.flow.Flows;
import org.manul.flow.Method;
import org.manul.object.instance.ColumnKind;
import org.manul.object.instance.core.ApiObject;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.Id;
import org.manul.object.type.Access;
import org.manul.object.type.ArrayKind;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;
import org.manul.util.*;

import java.util.*;

@Slf4j
public class ManulTest extends ManulTestBase {

    public void testMyList() {
        deploy("manul/List.mnl");
    }

    public void testShopping() {
        deploy("manul/shopping.mnl");
        // redeploy
        deploy("manul/shopping.mnl");
    }

    public void testLivingBeing() {
        deploy("manul/LivingBeing.mnl");
    }

    public void testUtils() {
        deploy("manul/util/Utils.mnl");
    }

    public void testGenericOverloading() {
        deploy("manul/GenericOverloading.mnl");
    }

    public void testLambda() {
        deploy("manul/Lambda.mnl");
        var r = callMethod("Utils", "findGt", List.of(List.of(1, 2, 3), 1));
        Assert.assertEquals(2, r);
    }

    public void testAssign() {
        deploy("manul/assign.mnl");
        var className = "Assign";
        var r = (int) callMethod(className, "test", List.of(1));
        Assert.assertEquals(2, r);
    }

    public void testConditional() {
        deploy("manul/conditional.mnl");
        var className = "Conditional";
        var r1 = (int) callMethod(className, "test", List.of(1));
        Assert.assertEquals(1, r1);
        var r2 = (int) callMethod(className, "test", List.of(-1));
        Assert.assertEquals(1, r2);
        var r3 = (String) callMethod(className, "test1", List.of(1));
        Assert.assertEquals("sub1", r3);
        var r4 = (String) callMethod(className, "test1", List.of(-1));
        Assert.assertEquals("sub2", r4);
    }

    public void testIntersectionType() {
        deploy("manul/intersection_type.mnl");
        var r = callMethod("Lab", "test", List.of());
        Assert.assertEquals("Hello Manul", r);
    }

    public void testCreateArray() {
        deploy("manul/CreateArray.mnl");
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance("Utils")));
            var method = klass.getMethod("createArray", List.of());
            var result = Flows.invoke(method.getRef(), null, List.of(), context);
            Assert.assertNotNull(result);
            var array = result.resolveArray();
            Assert.assertSame(ArrayKind.DEFAULT, array.getInstanceType().getKind());
        }
    }

    public void testInstanceOf() {
        deploy("manul/instanceof.mnl");
    }

    public void testUpdateField() {
        deploy("manul/update_field.mnl");
    }

//    public void testTreeSet() {
//        deploy("manul/tree_set.mnl");
//        var id = (Id) callMethod("TreeSetLab", "create", List.of());
//        var elements = List.of(5,4,3,2,1);
//        callMethod(id, "addAll", List.of(elements));
//        var containsAll = (boolean) callMethod(id, "containsAll", List.of(elements));
//        Assert.assertTrue(containsAll);
//        var removed = (boolean) callMethod(id, "remove", List.of(1));
//        Assert.assertTrue(removed);
//        var first = callMethod(id, "first", List.of());
//        Assert.assertEquals(2, first);
//        callMethod(id, "retainAll", List.of(List.of(5, 3, 2)));
//        var size = (int) callMethod(id, "size", List.of());
//        Assert.assertEquals(3, size);
//    }

    public void testSwapSuper() {
        deploy("manul/swap_super_before.mnl");
        var id = saveInstance("Derived", Map.of(
                "value1", 1, "value2", 2, "value3", 3
        ));
        deploy("manul/swap_super_after.mnl");
        Assert.assertEquals(
                2,
                callMethod(id, "getValue2", List.of())
        );
    }

    public void testImplicitSuperInit() {
        deploy("manul/implicit_super_init.mnl");
        var id = saveInstance("Sub", Map.of());
        var r = callMethod(id, "getValue", List.of());
        Assert.assertEquals(1, r);
    }

    public void testCircularReference() {
        deploy("manul/circular_ref.mnl");
        var id = saveInstance("Foo", Map.of());
        getObject(id);
    }

    public void testSmallInt() {
        deploy("manul/smallint.mnl");
        var className = "SmallIntFoo";
        Assert.assertEquals((short) 3, callMethod(className, "addShorts", List.of(1, 2)));
        Assert.assertEquals(3.0, callMethod(className, "addShortAndDouble", List.of(1, 2)));
    }

    public void testInnerKlass() {
        deploy("manul/inner_klass.mnl");
        var className = "Product";
        var productId = saveInstance(className, Map.of("quantity", 100));
        var product = getObject(productId);
        var inventory = getObject(product.getId("inventory"));
        Assert.assertEquals(100, inventory.getInt("quantity"));
    }

    public void testTag() {
        deploy("manul/tag.mnl");
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlassByQualifiedName("Foo");
            Assert.assertEquals((Integer) 1, klass.getSourceTag());
        }
    }

    public void testFieldInitializer() {
        deploy("manul/field_init.mnl");
        var id = saveInstance("FieldInit", Map.of());
        var r = callMethod(id, "getFlag", List.of());
        Assert.assertEquals(true, r);

        callMethod("FieldInit", "setDefaultFlag", List.of(false));
        var id1 = saveInstance("FieldInit", Map.of());
        var r1 = callMethod(id1, "getFlag", List.of());
        Assert.assertEquals(false, r1);
    }

    public void testImplicitInit() {
        deploy("manul/implicit_init.mnl");
        var id = saveInstance("ImplicitInit", Map.of());
        var value = callMethod(id, "getValue", List.of());
        Assert.assertEquals(1, value);
    }

    public void testMultiFiles() {
        deploy(List.of(
                "manul/multi_files.mnl",
                "manul/util/Utils.mnl"
                )
        );
        var value = callMethod("Lab", "test", List.of());
        Assert.assertEquals(true, value);
    }

    public void testForeach() {
        deploy("manul/foreach.mnl");
        var r = callMethod("ForeachLab", "sumList", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6, r);
        var r1 = callMethod("ForeachLab", "sumArray", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6, r1);
    }

    public void testBreak() {
        deploy("manul/break.mnl");
        var r = callMethod("BreakFoo", "contains", List.of(List.of(1, 2, 3), 2, 3));
        Assert.assertEquals(true, r);
        var r1 = callMethod("BreakFoo", "contains", List.of(List.of(1, 2, 3), 2, 1));
        Assert.assertEquals(false, r1);
    }

    public void testRange() {
        deploy("manul/range.mnl");
        var r = callMethod("RangeLab", "sum", List.of(10));
        Assert.assertEquals(45, r);
    }

    public void testContinue() {
        deploy("manul/continue.mnl");
        var r = callMethod("ContinueFoo", "avgEven", List.of(List.of(1, 2, 3, 4 ,5)));
        Assert.assertEquals(3, r);
    }

    public void testAnonymousClass() {
        deploy("manul/anonymous_class.mnl");
        var r = callMethod("AnonymousClassLab", "test", List.of());
        Assert.assertEquals(1, r);
    }

    public void testInnerClass() {
        deploy("manul/inner_class.mnl");
        var id = saveInstance("InnerClassLab", Map.of("value", 1));
        var r = callMethod(id, "getValue", List.of());
        assertEquals(1, r);
    }

    public void testLocalClass() {
        deploy("manul/local_class.mnl");
        var r = callMethod("LocalClassFoo", "test", List.of());
        assertEquals(1, r);
    }

    public void testNew() {
        deploy("manul/new.mnl");
        var id = (Id) callMethod("Foo", "test", List.of());
        var r = callMethod(id, "getValue", List.of());
        assertEquals(1, r);
    }

    public void testBindingVar() {
        deploy("manul/binding_var.mnl");
        var r = callMethod("BindingVarLab", "test", List.of("Hello"));
        assertEquals("Hello", r);
        var r1 = callMethod("BindingVarLab", "test", List.of(0));
        assertEquals("Manul", r1);
        var r2 = callMethod("BindingVarLab", "lengthEquals", List.of("Otoco", "Manul"));
        assertEquals(true, r2);
    }

    public void testMethodRef() {
        deploy("manul/method_ref.mnl");
        var r = callMethod("Lab", "test", List.of("Manul"));
        Assert.assertEquals("Hello, Manul", r);
    }

    public void testPrimInit() {
        deploy("manul/prim_init.mnl");
    }

    public void testWidening() {
        deploy("manul/widening.mnl");
        var id = saveInstance("Value", Map.of("value", 1));
        callMethod(id, "times", List.of(2));
        var value = getObject(id).getDouble("value");
        Assert.assertEquals(2.0, value, 0.0001);

        callMethod(id, "timesAndPlus", List.of(2, 1));
        var value1 = getObject(id).getDouble("value");
        Assert.assertEquals(5.0, value1, 0.0001);

        callMethod(id, "set", List.of(1));
        var value2 = getObject(id).getDouble("value");
        Assert.assertEquals(1.0, value2, 0.001);
    }

    public void testRequire() {
        deploy("manul/require.mnl");
        callMethod("Require", "requirePos", List.of(1));
        try {
            callMethod("Require", "requirePos", List.of(0));
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertEquals("Value must be positive", e.getMessage());
        }
    }

    public void testArrayForEach() {
        deploy("manul/arrays/ArrayLab.mnl");
        var fooIds = new ArrayList<Id>();
        for (int i = 0; i < 5; i++) {
            fooIds.add(saveInstance("arrays.Foo", Map.of("value", 1)));
        }
        callMethod("arrays.ArrayLab", "inc", List.of(fooIds));
        for (var fooId : fooIds) {
            var foo = getObject(fooId);
            Assert.assertEquals(2, foo.getInt("value"));
        }
    }

    public void testArrayMap() {
        deploy("manul/arrays/ArrayLab.mnl");
        var fooIds = new ArrayList<Id>();
        for (int i = 0; i < 5; i++) {
            fooIds.add(saveInstance("arrays.Foo", Map.of("value", 1)));
        }
        var r = callMethod("arrays.ArrayLab", "values", List.of(fooIds));
        Assert.assertEquals(List.of(1, 1, 1, 1, 1), r);
    }

    public void testArraySum() {
        deploy("manul/arrays/sum.mnl");
        var sum = (int) callMethod("arrays.Lab", "sumInt", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6, sum);
        var sum1 = (long) callMethod("arrays.Lab", "sumLong", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6L, sum1);
        var sum2 = (float) callMethod("arrays.Lab", "sumFloat", List.of(List.of(1.0, 2.0, 3.0)));
        Assert.assertEquals(6.0f, sum2, 0.001f);
        var sum3 = (double) callMethod("arrays.Lab", "sumDouble", List.of(List.of(1.0, 2.0, 3.0)));
        Assert.assertEquals(6.0, sum3, 0.001);
    }

    public void testCondExpr() {
        deploy("manul/condexpr/condexpr.mnl");
        var r = (double) callMethod("condexpr.Foo", "max", List.of(1.5, 1));
        Assert.assertEquals(1.5, r, 0.001);
    }

    public void testNullable() {
        deploy("manul/nullable/nullable.mnl");
        var args = new HashMap<String, Object>();
        args.put("value", null);
        var r = (int) callMethod("nullable.NullableFoo", "test", args);
        Assert.assertEquals(0, r);
        var r1 = (int) callMethod("nullable.NullableFoo", "test", Map.of());
        Assert.assertEquals(0, r1);
    }

    public void testInnerEnum() {
        deploy("manul/enums/inner_enum.mnl");
        var id = saveInstance("enums.Foo",
                Map.of("option", ApiNamedObject.of("enums.Foo.Option", "op1"))
        );
        var foo = getObject(id);
        Assert.assertEquals("op1", foo.getEnumConstant("option").name());
    }

    public void testChildren() {
        deploy("manul/children.mnl");
        var id = saveInstance("Product", Map.of(
                "name", "Shoes",
                        "SKU", List.of(
                                Map.of(
                                        "variant", "40",
                                        "price", 100,
                                        "stock", 100
                                )
                        )

                )
        );
        var product = getObject(id);
        var skus = product.getChildren("SKU");
        Assert.assertEquals(1, skus.size());
        var sku = skus.getFirst();
        Assert.assertEquals("40", sku.getString("variant"));
        Assert.assertEquals(100, sku.getDouble("price"), 0.001);
        Assert.assertEquals(100, sku.getInt("stock"));
    }

    public void testSearch() {
        deploy("manul/search/search.mnl");
        var className = "search.SearchFoo";
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            context.loadKlasses();
            var klass = context.getKlassByQualifiedName(className);
            var field = klass.getFields().getFirst();
            var column = field.getColumn();
            Assert.assertSame(ColumnKind.STRING, column.kind());
        }
        var id = saveInstance(className, Map.of("name", "manul",
                "Child", List.of(
                        Map.of(
                                "name", "child"
                        )
                )
        ));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = search(className, Map.of(
                "name", "manul",
                "status", ApiNamedObject.of("search.Status", "ENABLED")
        ));
        Assert.assertEquals(1, r.total());
        Assert.assertEquals(id, r.items().getFirst().id());
        assertNull(r.items().getFirst().getMap().get("children"));

        var id1 = saveInstance(className, Map.of("name", "manul", "Child", List.of(
                        Map.of(
                                "name", "child"
                        )
                ))
        );
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r1 = search(className, Map.of());
        assertEquals(2, r1.total());
        assertEquals(id1, r1.items().getFirst().id());
    }

    public void testBean() {
        deploy("manul/beans/foo_service.mnl");
        var r = callMethod(ApiNamedObject.of("fooService"), "greet", List.of());
        Assert.assertEquals("Hello", r);
    }

    public void testDisplay() {
        deploy("manul/display/display.mnl");
        try (var context = newContext()) {
            context.loadKlasses();
            var productCls = context.getKlassByQualifiedName("display.Product");
            assertEquals(1, productCls.getAttributes().size());
            var labelAttr = productCls.getAttributes().getFirst();
            assertEquals("label", labelAttr.name());
            assertEquals("Product", labelAttr.value());

            assertEquals(3, productCls.getFieldCount());

            var nameField = productCls.getFields().getFirst();
            assertEquals(List.of(new Attribute("label", "Product Name")), nameField.getAttributes());

            var priceField = productCls.getFields().get(1);
            assertEquals(List.of(new Attribute("label", "Product Price")), priceField.getAttributes());

            var stockField = productCls.getFields().get(2);
            assertEquals(List.of(new Attribute("label", "Product Stock")), stockField.getAttributes());

            assertSame(nameField, productCls.getTitleField());

            assertEquals(2, productCls.getMethods().size());
            var init = productCls.getMethods().getFirst();

            assertEquals(List.of(new Attribute("label", "Product Name")), init.getParameter(0).getAttributes());
            assertEquals(List.of(new Attribute("label", "Product Price")), init.getParameter(1).getAttributes());
            assertEquals(List.of(new Attribute("label", "Product Stock")), init.getParameter(2).getAttributes());

            var reduceStockMethod = productCls.getMethods().get(1);
            assertEquals(List.of(new Attribute("label", "Remove Product Stock")), reduceStockMethod.getAttributes());

            assertEquals(List.of(new Attribute("label", "Removed Quantity")), reduceStockMethod.getParameter(0).getAttributes());

            var categoryCls = context.getKlassByQualifiedName("display.Category");
            var enumConsts = categoryCls.getEnumConstants();
            assertEquals(List.of(new Attribute("label", "Electronics")), enumConsts.getFirst().getAttributes());
            assertEquals(List.of(new Attribute("label", "Clothing")), enumConsts.get(1).getAttributes());
            assertEquals(List.of(new Attribute("label", "Other")), enumConsts.get(2).getAttributes());
        }
    }

    public void testAccess() {
        deploy("manul/access/access.mnl");
        try (var context = newContext()) {
            context.loadKlasses();
            var klass = context.getKlassByQualifiedName("access.Product");
            var field = klass.getFieldByName("stock");
            assertSame(Access.PRIVATE, field.getAccess());
            var m = Utils.findRequired(klass.getMethods(), m1 -> m1.getName().equals("__stock__"));
            assertSame(Access.PRIVATE, m.getAccess());
        }
        var id = saveInstance("access.Product", Map.of("stock", 100));
        var product = getObject(id);
        assertNull(product.get("stock"));
    }

    public void testString() {
        deploy("manul/str/str.mnl");
        var r = callMethod("str.StringLab", "concat", List.of("Hello", " Manul!"));
        assertEquals("Hello Manul!", r);

        var r1 = callMethod("str.StringLab", "toStr", List.of(1));
        assertEquals("1", r1);

        var r2 = callMethod("str.StringLab", "toStr", List.of(1.0));
        assertEquals("1.0", r2);

        var r3 = callMethod("str.StringLab", "concat", List.of("No.", 1));
        assertEquals("No. 1", r3);

    }

    public void testDelete() {
        deploy("manul/del/del.mnl");
        var id = saveInstance("del.Foo", Map.of("name", "foo"));
        callMethod(id, "del", List.of());
        try {
            getObject(id);
            fail("Retrival should have failed");
        }
        catch (BusinessException e) {
            assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        var fooId1 = saveInstance("del.Foo", Map.of("name", "foo1"));
        saveInstance("del.Bar", Map.of("foo", fooId1));
        try (var context = newContext()) {
            context.loadKlasses();
            var foo1 = (ClassInstance) context.get(fooId1);
            assertEquals(1, foo1.getRefcount());
        }
        try {
            callMethod(fooId1, "del", List.of());
            fail("Deletion should have failed");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
        }
    }

    public void testValueClass() {
        deploy("manul/value/value.mnl");
        var id = saveInstance("value.Product", Map.of(
           "name", "Shoes",
           "price", Map.of(
                   "amount", 100,
                        "currency",
                        ApiNamedObject.of("value.Currency", "CNY")
                )
        ));
        var product = getObject(id);
        var price = product.get("price");
        MatcherAssert.assertThat(price, CoreMatchers.instanceOf(ApiObject.class));
    }

    public void testInvalidSummaryType() {
        try {
            deploy("manul/error/invalid_summary_type.mnl");
            fail("Should have failed");
        }
        catch (CompilationException ignored) {}
    }

    public void testWideningToNullable() {
        deploy("manul/widening/nullable.mnl");
        var id = (Id) callMethod(
                ApiNamedObject.of("fooService"),
                "create",
                List.of(1)
        );
        assertEquals(1.0, getObject(id).getFloat("rate"), 0.01);
    }

    public void testFrozen() {
        deploy("manul/ident/semi_keyword.mnl");
    }

    public void testIndexEntryRemoval() {
        deploy("manul/index/remove.mnl");
        var id = saveInstance("index.Foo", Map.of("name", "foo"));
        var fooService = ApiNamedObject.of("fooService");
        var found = callMethod(fooService, "findFooByName", List.of("foo"));
        assertEquals(id, found);
        deleteObject(id);
        try {
            getObject(id);
            fail("Should have failed");
        } catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        var found1 = callMethod(fooService, "findFooByName", List.of("foo"));
        assertNull(found1);
    }

    public void testChildIndexEntryRemoval() {
        deploy("manul/index/remove_child.mnl");
        var id = saveInstance("index.Foo", Map.of(
                "Child", List.of(
                        Map.of(
                        "name", "child"
                        )
                ))
        );
        var childId = getObject(id).getChildren("Child").getFirst().id();
        var childService = ApiNamedObject.of("childService");
        var found = callMethod(childService, "findChildByName", List.of("child"));
        assertEquals(childId, found);
        deleteObject(childId);
        try {
            getObject(childId);
            fail("Should have failed");
        } catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        var found1 = callMethod(childService, "findChildByName", List.of("child"));
        assertNull(found1);
    }

    public void testUniqueIndex() {
        deploy("manul/index/unique.mnl");
        var className = "unique.Foo";
        var id1 = saveInstance(className, Map.of("name", "foo1"));
        var bean = ApiNamedObject.of("fooService");
        var found = callMethod(bean, "findByName", List.of("foo1"));
        assertEquals(id1, found);

        try {
            saveInstance(className, Map.of("name", "foo1"));
            fail("Creation of duplicate index entries is not prevented");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.CONSTRAINT_CHECK_FAILED, e.getErrorCode());
        }

        var id2 = saveInstance(className, Map.of("name", "foo2"));
        callMethod(bean, "swapNames", List.of(id1, id2));
        var foo1 = getObject(id1);
        var foo2 = getObject(id2);
        assertEquals("foo2", foo1.getString("name"));
        assertEquals("foo1", foo2.getString("name"));

        var id3 = (Id) callMethod(bean, "deleteAndNew", List.of(id1));
        var foo3 = getObject(id3);
        assertEquals("foo2", foo3.getString("name"));
    }

    public void testIndexGetAll() {
        deploy("manul/index/get_all.mnl");
        var className = "index.Product";
        var ids = new ArrayList<Id>();
        for (int i = 0; i < 3; i++) {
            ids.add(saveInstance(className, Map.of("name", "Shoes")));
        }
        var r = (List<?>) callMethod(ApiNamedObject.of("productService"), "findAllByName", List.of("Shoes"));
        assertEquals(ids, r);
    }

    public void testImplicitChildDelete() {
        deploy("manul/children/implicit_child_del.mnl");
        var id = saveInstance("children.Parent", Map.of(
                "Child", List.of(Map.of(
                        "seq", 1
                ))
        ));
        var parent = getObject(id);
        assertEquals(1, parent.getChildren("Child").size());
        saveInstance("children.Parent", Map.of(
                "$id", id,
                "Child", List.of()
        ));
        var parent1 = getObject(id);
        assertEquals(0, parent1.getChildren("Child").size());
    }

    public void testLineComment() {
        deploy("manul/comment/line_comment.mnl");
        var id = saveInstance("comment.Foo", Map.of("name", "foo"));
        var foo = getObject(id);
        assertEquals("foo", foo.get("name"));
    }

    public void testMultiLineComment() {
        deploy("manul/comment/multi_line_comment.mnl");
        var id = saveInstance("comment.Foo", Map.of("name", "foo"));
        var foo = getObject(id);
        assertEquals("foo", foo.get("name"));
    }

    public void testLambdaForwardFieldRef() {
        deploy("manul/lambda/forward_field_ref.mnl");
    }

    public void testNewIndexNewField() {
        deploy("manul/ddl/new_idx_new_field_before.mnl");
        var id = saveInstance("ddl.WorkOrder", Map.of("orderNo", "001"));
        deploy("manul/ddl/new_idx_new_field_after.mnl");
        var wd = getObject(id);
        var locId = wd.getId("targetLocation");
        var loc = getObject(locId);
        assertEquals("Default Location", loc.get("name"));
    }

//    public void testBugfix() {
//        deploy("manul/bugfix.mnl");
//        var customerId = saveInstance("Customer", Map.of(
//                "name", "Leen",
//                "email", "leen@manul.org",
//                "passwordHash", "123456",
//                "address", "Manul Village 001",
//                "phone", "111"
//        ));
//        var restaurantId = saveInstance("Restaurant", Map.of(
//                "name", "Pizza Hut",
//                "address", "Manul Village 002",
//                "cuisine", ApiNamedObject.of(
//                        "Cuisine",
//                        "ITALIAN"
//                )
//        ));
//        var orderId = saveInstance("Order", Map.of(
//                "customer", customerId,
//                "restaurant", restaurantId,
//                "totalPrice", Map.of(
//                        "amount", 100,
//                        "currency",ApiNamedObject.of("Currency", "USD")
//                )
//        ));
//        var order = getObject(orderId);
//        var summary = order.get("summary");
//        logger.debug("{}", summary);
//    }

    private void rebuildNodes(Klass clazz) {
        for (Method method : clazz.getMethods()) {
            method.rebuildNodes();
            for (Klass klass : method.getKlasses()) {
                rebuildNodes(klass);
            }
        }
        for (Klass klass : clazz.getKlasses()) {
            rebuildNodes(klass);
        }
    }


    public void testEsFieldName() {
        deploy("manul/search/es_field_name.mnl");
        try (var context = newContext()) {
            context.loadKlasses();
            var cls = context.getKlassByQualifiedName("search.Product");
            var field = cls.getFieldByName("name");
            assertEquals("s0", field.getColumn().name());
        }
    }

    public void testDeleteInBranch() {
        deploy("manul/del/del_in_branch.mnl");
        var id = saveInstance("del.Inventory", Map.of(
                "quantity", 100
        ));
        callMethod(id, "reduceQuantity", List.of(100));
        try {
            getObject(id);
            fail("Instance should have been deleted");
        } catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
    }

    public void testConcatValueObject() {
        deploy("manul/str/concat_value_obj.mnl");
        var customerId = saveInstance("str.Customer", Map.of("name", "Leen"));
        var orderId = saveInstance("str.Order", Map.of("customer", customerId, "totalPrice",
                Map.of("amount", 100, "currency", ApiNamedObject.of("str.Currency", "CNY"))
                ));
        var order = getObject(orderId);
        assertEquals("Order: Leen-¥100.0", order.get("summary"));
    }

    public void testImplicitNullInitialValue() {
        deploy("manul/field_init/implicit_null_initial_value.mnl");
        var id = saveInstance("field_init.Foo", Map.of());
        var foo = getObject(id);
        assertNull(foo.get("value"));
    }

    public void testTimeNow() {
        deploy("manul/time/now.mnl");
        var r = (boolean) callMethod(ApiNamedObject.of("lab"), "beforeNow", List.of(
                System.currentTimeMillis()
        ));
        assertTrue(r);
    }

    public void testEnumConstType() {
        deploy("manul/enums/enum_const_type.mnl");
        try (var context = newContext()) {
            context.loadKlasses();
            var productKlass = context.getKlassByQualifiedName("enums.Product");
            var statusKlass = context.getKlassByQualifiedName("enums.ProductStatus");
            var f = productKlass.getFieldByName("status");
            assertEquals(statusKlass.getType(), f.getType());
            for (Field ecField : statusKlass.getEnumConstants()) {
                var ec = ecField.getStatic(context);
                var ecKlass = statusKlass.getKlassByByName("$" + ecField.getName());
                assertEquals(ecKlass.getType(), ec.resolveMvObject().getInstanceType());
            }
        }
    }

}
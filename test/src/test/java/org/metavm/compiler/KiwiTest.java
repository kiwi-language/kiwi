package org.metavm.compiler;

import org.junit.Assert;
import org.metavm.flow.Flows;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KiwiTest extends KiwiTestBase {

    public static final Logger logger = LoggerFactory.getLogger(KiwiTest.class);

    public void testParentChild() {
        deploy("kiwi/ParentChild.kiwi");
    }

    public void testMyList() {
        deploy("kiwi/List.kiwi");
    }

    public void testShopping() {
        deploy("kiwi/Shopping.kiwi");
        // redeploy
        deploy("kiwi/Shopping.kiwi");
    }

    public void testLivingBeing() {
        deploy("kiwi/LivingBeing.kiwi");
    }

    public void testUtils() {
        deploy("kiwi/util/Utils.kiwi");
    }

    public void testGenericOverloading() {
        deploy("kiwi/GenericOverloading.kiwi");
    }

    public void testLambda() {
        deploy("kiwi/Lambda.kiwi");
        var r = callMethod("Utils", "findGt", List.of(List.of(1, 2, 3), 1));
        Assert.assertEquals(2, r);
    }

    public void testAssign() {
        deploy("kiwi/assign.kiwi");
        var className = "Assign";
        var r = (int) callMethod(className, "test", List.of(1));
        Assert.assertEquals(2, r);
    }

    public void testConditional() {
        deploy("kiwi/conditional.kiwi");
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
        deploy("kiwi/intersection_type.kiwi");
        var r = callMethod("Lab", "test", List.of());
        Assert.assertEquals("Hello Kiwi", r);
    }

    public void testCreateArray() {
        deploy("kiwi/CreateArray.kiwi");
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
        deploy("kiwi/instanceof.kiwi");
    }

    public void testUpdateField() {
        deploy("kiwi/update_field.kiwi");
    }

    public void testTreeSet() {
        deploy("kiwi/tree_set.kiwi");
        var id = (String) callMethod("TreeSetLab", "create", List.of());
        var elements = List.of(5,4,3,2,1);
        callMethod(id, "addAll", List.of(elements));
        var containsAll = (boolean) callMethod(id, "containsAll", List.of(elements));
        Assert.assertTrue(containsAll);
        var removed = (boolean) callMethod(id, "remove", List.of(1));
        Assert.assertTrue(removed);
        var first = callMethod(id, "first", List.of());
        Assert.assertEquals(2, first);
        callMethod(id, "retainAll", List.of(List.of(5, 3, 2)));
        var size = (int) callMethod(id, "size", List.of());
        Assert.assertEquals(3, size);
    }

    public void testSwapSuper() {
        deploy("kiwi/swap_super_before.kiwi");
        var id = saveInstance("Derived", Map.of(
                "value1", 1, "value2", 2, "value3", 3
        ));
        deploy("kiwi/swap_super_after.kiwi");
        Assert.assertEquals(
                2,
                callMethod(id, "getValue2", List.of())
        );
    }

    public void testImplicitSuperInit() {
        deploy("kiwi/implicit_super_init.kiwi");
        var id = saveInstance("Sub", Map.of());
        var r = callMethod(id, "getValue", List.of());
        Assert.assertEquals(1, r);
    }

    public void testCircularReference() {
        deploy("kiwi/circular_ref.kiwi");
        var id = saveInstance("Foo", Map.of());
        getObject(id);
    }

    public void testSmallInt() {
        deploy("kiwi/smallint.kiwi");
        var className = "SmallIntFoo";
        Assert.assertEquals((short) 3, callMethod(className, "addShorts", List.of(1, 2)));
        Assert.assertEquals(3.0, callMethod(className, "addShortAndDouble", List.of(1, 2)));
    }

    public void testInnerKlass() {
        deploy("kiwi/inner_klass.kiwi");
        var className = "Product";
        var productId = saveInstance(className, Map.of("quantity", 100));
        var product = getObject(productId);
        var inventory = getObject(product.getString("inventory"));
        Assert.assertEquals(100, inventory.getInt("quantity"));
    }

    public void testTag() {
        deploy("kiwi/tag.kiwi");
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlassByQualifiedName("Foo");
            Assert.assertEquals((Integer) 1, klass.getSourceTag());
        }
    }

    public void testFieldInitializer() {
        deploy("kiwi/field_init.kiwi");
        var id = saveInstance("FieldInit", Map.of());
        var r = callMethod(id, "getFlag", List.of());
        Assert.assertEquals(true, r);

        callMethod("FieldInit", "setDefaultFlag", List.of(false));
        var id1 = saveInstance("FieldInit", Map.of());
        var r1 = callMethod(id1, "getFlag", List.of());
        Assert.assertEquals(false, r1);
    }

    public void testImplicitInit() {
        deploy("kiwi/implicit_init.kiwi");
        var id = saveInstance("ImplicitInit", Map.of());
        var value = callMethod(id, "getValue", List.of());
        Assert.assertEquals(1, value);
    }

    public void testMultiFiles() {
        deploy(List.of(
                "kiwi/multi_files.kiwi",
                "kiwi/util/Utils.kiwi"
                )
        );
        var value = callMethod("Lab", "test", List.of());
        Assert.assertEquals(true, value);
    }

    public void testForeach() {
        deploy("kiwi/foreach.kiwi");
        var r = callMethod("ForeachLab", "sumList", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6, r);
        var r1 = callMethod("ForeachLab", "sumArray", List.of(List.of(1, 2, 3)));
        Assert.assertEquals(6, r1);
    }

    public void testBreak() {
        deploy("kiwi/break.kiwi");
        var r = callMethod("BreakFoo", "contains", List.of(List.of(1, 2, 3), 2, 3));
        Assert.assertEquals(true, r);
        var r1 = callMethod("BreakFoo", "contains", List.of(List.of(1, 2, 3), 2, 1));
        Assert.assertEquals(false, r1);
    }

    public void testRange() {
        deploy("kiwi/range.kiwi");
        var r = callMethod("RangeLab", "sum", List.of(10));
        Assert.assertEquals(45, r);
    }

    public void testContinue() {
        deploy("kiwi/continue.kiwi");
        var r = callMethod("ContinueFoo", "avgEven", List.of(List.of(1, 2, 3, 4 ,5)));
        Assert.assertEquals(3, r);
    }

    public void testAnonymousClass() {
        deploy("kiwi/anonymous_class.kiwi");
        var r = callMethod("AnonymousClassLab", "test", List.of());
        Assert.assertEquals(1, r);
    }

    public void testInnerClass() {
        deploy("kiwi/inner_class.kiwi");
        var id = saveInstance("InnerClassLab", Map.of("value", 1));
        var r = callMethod(id, "getValue", List.of());
        assertEquals(1, r);
    }

    public void testLocalClass() {
        deploy("kiwi/local_class.kiwi");
        var r = callMethod("LocalClassFoo", "test", List.of());
        assertEquals(1, r);
    }

    public void testNew() {
        deploy("kiwi/new.kiwi");
        var id = (String) callMethod("Foo", "test", List.of());
        var r = callMethod(id, "getValue", List.of());
        assertEquals(1, r);
    }

    public void testBindingVar() {
        deploy("kiwi/binding_var.kiwi");
        var r = callMethod("BindingVarLab", "test", List.of("Hello"));
        assertEquals("Hello", r);
        var r1 = callMethod("BindingVarLab", "test", List.of(1));
        assertEquals("Kiwi", r1);
        var r2 = callMethod("BindingVarLab", "lengthEquals", List.of("Cute", "Kiwi"));
        assertEquals(true, r2);
    }

    public void testMethodRef() {
        deploy("kiwi/method_ref.kiwi");
        var r = callMethod("Lab", "test", List.of("Kiwi"));
        Assert.assertEquals("Hello, Kiwi", r);
    }

    public void testPrimInit() {
        deploy("kiwi/prim_init.kiwi");
    }

    public void testWidening() {
        deploy("kiwi/widening.kiwi");
        var id = saveInstance("Value", Map.of("value", 1));
        callMethod(id, "times", List.of(2));
        var value = getObject(id).getDouble("value");
        Assert.assertEquals(2.0, value, 0.0001);
    }

}